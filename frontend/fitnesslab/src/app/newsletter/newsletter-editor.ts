import { Component, OnInit, OnDestroy, inject, signal, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { Editor, Node } from '@tiptap/core';
import Document from '@tiptap/extension-document';
import Paragraph from '@tiptap/extension-paragraph';
import Text from '@tiptap/extension-text';
import Gapcursor from '@tiptap/extension-gapcursor';
import Bold from '@tiptap/extension-bold';
import Italic from '@tiptap/extension-italic';
import Strike from '@tiptap/extension-strike';
import Heading from '@tiptap/extension-heading';
import BulletList from '@tiptap/extension-bullet-list';
import OrderedList from '@tiptap/extension-ordered-list';
import ListItem from '@tiptap/extension-list-item';
import Blockquote from '@tiptap/extension-blockquote';
import HardBreak from '@tiptap/extension-hard-break';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';
import Underline from '@tiptap/extension-underline';
import { TextStyle } from '@tiptap/extension-text-style';
import { Color } from '@tiptap/extension-color';
import FontFamily from '@tiptap/extension-font-family';
import Highlight from '@tiptap/extension-highlight';
import { Table } from '@tiptap/extension-table';
import { TableRow } from '@tiptap/extension-table-row';
import { TableCell } from '@tiptap/extension-table-cell';
import { TableHeader } from '@tiptap/extension-table-header';
import { NewsletterService } from './newsletter.service';

@Component({
  selector: 'gym-newsletter-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  styles: [`
    .editor-toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 0.25rem;
      padding: 0.5rem;
      background: rgb(51, 65, 85);
      border: 1px solid rgb(71, 85, 105);
      border-bottom: none;
      border-radius: 0.5rem 0.5rem 0 0;
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .editor-toolbar button {
      padding: 0.5rem 0.75rem;
      background: rgb(71, 85, 105);
      color: rgb(248, 250, 252);
      border: none;
      border-radius: 0.25rem;
      cursor: pointer;
      font-size: 0.875rem;
      font-weight: 500;
      transition: background-color 0.2s;
      min-width: 2.5rem;
    }

    .editor-toolbar button:hover {
      background: rgb(100, 116, 139);
    }

    .editor-toolbar button.is-active {
      background: rgb(59, 130, 246);
    }

    .editor-toolbar button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .editor-toolbar .divider {
      width: 1px;
      background: rgb(100, 116, 139);
      margin: 0 0.25rem;
    }

    .editor-content {
      background: white;
      color: black;
      border: 1px solid rgb(71, 85, 105);
      border-radius: 0 0 0.5rem 0.5rem;
      min-height: 600px;
      padding: 2rem;
      overflow-y: auto;
      max-height: calc(100vh - 400px);
    }

    input[type="file"] {
      display: none;
    }
  `],
  template: `
    <div class="p-3 sm:p-5 max-w-7xl mx-auto">
      <div class="flex flex-col gap-4 mb-5">
        <div class="flex justify-between items-center">
          <h2 class="text-2xl sm:text-3xl font-bold text-slate-50">
            {{ isNewMode ? ('newsletter.createNewsletter' | translate) : ('newsletter.editNewsletter' | translate) }}
          </h2>
          <button
            (click)="goBack()"
            class="px-4 py-2 bg-slate-600 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-slate-700 transition-colors"
          >
            {{ 'common.back' | translate }}
          </button>
        </div>

        <div class="bg-slate-800 shadow-md rounded-lg p-4 sm:p-6">
          <div class="mb-4">
            <label for="title" class="block text-sm font-medium text-slate-300 mb-2">
              {{ 'newsletter.title' | translate }}
            </label>
            <input
              id="title"
              type="text"
              [(ngModel)]="title"
              placeholder="{{ 'newsletter.titlePlaceholder' | translate }}"
              class="w-full px-3 py-2 border border-slate-600 rounded focus:outline-none focus:ring-2 focus:ring-blue-400 bg-slate-700 text-slate-50"
            />
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-slate-300 mb-2">
              {{ 'newsletter.content' | translate }}
            </label>

            <!-- Template Selector -->
            <div class="mb-3 flex gap-2 flex-wrap">
              <button
                (click)="showTemplates.set(!showTemplates())"
                class="px-3 py-2 bg-slate-700 text-slate-50 rounded border border-slate-600 hover:bg-slate-600 text-sm"
                type="button"
              >
                📄 Load Template
              </button>
              <button
                (click)="openSaveTemplateDialog()"
                class="px-3 py-2 bg-slate-700 text-slate-50 rounded border border-slate-600 hover:bg-slate-600 text-sm"
                type="button"
              >
                💾 Save as Template
              </button>
            </div>

            @if (showTemplates()) {
              <div class="mb-3 p-3 bg-slate-700 rounded border border-slate-600">
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
                  @for (template of templates(); track template.id) {
                    <button
                      (click)="loadTemplate(template.id)"
                      class="p-3 bg-slate-800 rounded border border-slate-600 hover:border-blue-500 text-left transition-colors"
                      type="button"
                    >
                      <div class="font-semibold text-slate-50">{{ template.name }}</div>
                      <div class="text-xs text-slate-400 mt-1">{{ template.description }}</div>
                    </button>
                  }
                </div>
              </div>
            }

            <!-- Editor Toolbar -->
            <div class="editor-toolbar">
              <button (click)="toggleBold()" [class.is-active]="isActive('bold')" type="button" title="Bold">
                <strong>B</strong>
              </button>
              <button (click)="toggleItalic()" [class.is-active]="isActive('italic')" type="button" title="Italic">
                <em>I</em>
              </button>
              <button (click)="toggleUnderline()" [class.is-active]="isActive('underline')" type="button" title="Underline">
                <u>U</u>
              </button>
              <button (click)="toggleStrike()" [class.is-active]="isActive('strike')" type="button" title="Strikethrough">
                <s>S</s>
              </button>

              <span class="divider"></span>

              <button (click)="setHeading(1)" [class.is-active]="isActive('heading', { level: 1 })" type="button" title="Heading 1">
                H1
              </button>
              <button (click)="setHeading(2)" [class.is-active]="isActive('heading', { level: 2 })" type="button" title="Heading 2">
                H2
              </button>
              <button (click)="setHeading(3)" [class.is-active]="isActive('heading', { level: 3 })" type="button" title="Heading 3">
                H3
              </button>
              <button (click)="setParagraph()" [class.is-active]="isActive('paragraph')" type="button" title="Paragraph">
                P
              </button>

              <span class="divider"></span>

              <button (click)="setAlign('left')" [class.is-active]="isActive({ textAlign: 'left' })" type="button" title="Align Left">
                ⬅
              </button>
              <button (click)="setAlign('center')" [class.is-active]="isActive({ textAlign: 'center' })" type="button" title="Align Center">
                ↔
              </button>
              <button (click)="setAlign('right')" [class.is-active]="isActive({ textAlign: 'right' })" type="button" title="Align Right">
                ➡
              </button>

              <span class="divider"></span>

              <button (click)="toggleBulletList()" [class.is-active]="isActive('bulletList')" type="button" title="Bullet List">
                • List
              </button>
              <button (click)="toggleOrderedList()" [class.is-active]="isActive('orderedList')" type="button" title="Numbered List">
                1. List
              </button>
              <button (click)="toggleBlockquote()" [class.is-active]="isActive('blockquote')" type="button" title="Quote">
                "
              </button>

              <span class="divider"></span>

              <div style="position: relative; display: inline-block;">
                <button (click)="showColorPicker.set(!showColorPicker())" type="button" title="Text Color">
                  🎨 Color
                </button>
                @if (showColorPicker()) {
                  <input
                    type="color"
                    [(ngModel)]="currentColor"
                    (change)="setTextColor(currentColor)"
                    style="position: absolute; top: 100%; left: 0; z-index: 20;"
                  />
                }
              </div>

              <div style="position: relative; display: inline-block;">
                <button (click)="showHighlightPicker.set(!showHighlightPicker())" type="button" title="Highlight">
                  ✨ Highlight
                </button>
                @if (showHighlightPicker()) {
                  <input
                    type="color"
                    [(ngModel)]="currentHighlight"
                    (change)="setHighlight(currentHighlight)"
                    style="position: absolute; top: 100%; left: 0; z-index: 20;"
                  />
                }
              </div>

              <select (change)="setFontFamily($event)" class="px-2 py-1 bg-slate-700 text-slate-50 rounded border border-slate-600" title="Font Family">
                <option value="">Font</option>
                <option value="Arial">Arial</option>
                <option value="Helvetica">Helvetica</option>
                <option value="Times New Roman">Times New Roman</option>
                <option value="Georgia">Georgia</option>
                <option value="Courier New">Courier New</option>
                <option value="Verdana">Verdana</option>
              </select>

              <span class="divider"></span>

              <button (click)="insertTable()" type="button" title="Insert Table">
                📊 Table
              </button>

              <span class="divider"></span>

              <button (click)="triggerImageUpload()" type="button" title="Upload Image">
                📷 Upload
              </button>
              <button (click)="addImageUrl()" type="button" title="Image from URL">
                🔗 Image URL
              </button>
              <button (click)="addLink()" type="button" title="Add Link">
                🔗 Link
              </button>

              <span class="divider"></span>

              <button (click)="toggleHtmlView()" type="button" [class.is-active]="showHtml()" title="Toggle HTML/Visual">
                {{ showHtml() ? '👁 Visual' : '< > HTML' }}
              </button>

              <button (click)="togglePreview()" type="button" [class.is-active]="showPreview()" title="Preview Newsletter">
                👀 Preview
              </button>
            </div>

            <!-- Hidden file input for image upload -->
            <input
              #fileInput
              type="file"
              accept="image/*"
              (change)="onImageUpload($event)"
            />

            <!-- Editor Content -->
            @if (showPreview()) {
              <div class="border border-slate-600 rounded-b-lg" style="border-radius: 0 0 0.5rem 0.5rem;">
                <div class="bg-slate-700 px-4 py-2 text-slate-300 text-sm">
                  Preview Mode - This is how your newsletter will look
                </div>
                <div class="bg-white p-4 min-h-[600px] overflow-auto" [innerHTML]="getPreviewContent()"></div>
              </div>
            } @else if (!showHtml()) {
              <div class="editor-content" #editorElement></div>
            } @else {
              <textarea
                [(ngModel)]="htmlContent"
                (ngModelChange)="onHtmlChange($event)"
                class="w-full min-h-[600px] p-4 border border-slate-600 rounded-b-lg bg-slate-900 text-slate-50 font-mono text-sm"
                style="border-radius: 0 0 0.5rem 0.5rem;"
              ></textarea>
            }
          </div>

          <div class="flex gap-3 justify-end">
            <button
              (click)="goBack()"
              class="px-4 py-2 bg-slate-600 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-slate-700 transition-colors"
            >
              {{ 'common.cancel' | translate }}
            </button>
            <button
              (click)="save()"
              class="px-4 py-2 bg-blue-500 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-blue-600 transition-colors"
            >
              {{ 'common.save' | translate }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class NewsletterEditor implements OnInit, OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private newsletterService = inject(NewsletterService);
  private sanitizer = inject(DomSanitizer);

  editor: Editor | null = null;
  title = '';
  htmlContent = '';
  showHtml = signal(false);
  showTemplates = signal(false);
  showPreview = signal(false);
  isNewMode = true;
  newsletterId?: string;
  showColorPicker = signal(false);
  showHighlightPicker = signal(false);
  currentColor = '#000000';
  currentHighlight = '#ffff00';
  templates = this.newsletterService.getTemplates();

  // Custom Div extension to preserve div containers with inline styles
  Div = Node.create({
    name: 'div',
    group: 'block',
    content: 'block+',
    defining: true,

    addAttributes() {
      return {
        style: {
          default: null,
          parseHTML: element => element.getAttribute('style'),
          renderHTML: attributes => {
            if (!attributes['style']) {
              return {};
            }
            return { style: attributes['style'] };
          },
        },
      };
    },

    parseHTML() {
      return [{ tag: 'div' }];
    },

    renderHTML({ HTMLAttributes }) {
      return ['div', HTMLAttributes, 0];
    },
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.isNewMode = id === 'new';

    if (!this.isNewMode && id) {
      this.newsletterId = id;
      const newsletter = this.newsletterService.getNewsletterById(id);
      if (newsletter) {
        this.title = newsletter.title;
        this.htmlContent = newsletter.content;
      }
    }

    // Initialize TipTap editor
    setTimeout(() => {
      const editorElement = document.querySelector('.editor-content');
      if (editorElement) {
        this.initializeEditor(editorElement as HTMLElement);
      }
    }, 0);
  }

  private initializeEditor(editorElement: HTMLElement): void {
    this.editor = new Editor({
          element: editorElement as HTMLElement,
          extensions: [
            Document,
            this.Div,
            Paragraph.extend({
              addAttributes() {
                return {
                  ...this.parent?.(),
                  style: {
                    default: null,
                    parseHTML: element => element.getAttribute('style'),
                    renderHTML: attributes => {
                      if (!attributes['style']) {
                        return {};
                      }
                      return { style: attributes['style'] };
                    },
                  },
                };
              },
            }),
            Text,
            Bold,
            Italic,
            Strike,
            Underline,
            Heading.extend({
              addAttributes() {
                return {
                  ...this.parent?.(),
                  style: {
                    default: null,
                    parseHTML: element => element.getAttribute('style'),
                    renderHTML: attributes => {
                      if (!attributes['style']) {
                        return {};
                      }
                      return { style: attributes['style'] };
                    },
                  },
                };
              },
            }).configure({
              levels: [1, 2, 3, 4, 5, 6],
            }),
            BulletList,
            OrderedList,
            ListItem,
            Blockquote.extend({
              addAttributes() {
                return {
                  ...this.parent?.(),
                  style: {
                    default: null,
                    parseHTML: element => element.getAttribute('style'),
                    renderHTML: attributes => {
                      if (!attributes['style']) {
                        return {};
                      }
                      return { style: attributes['style'] };
                    },
                  },
                };
              },
            }),
            HardBreak,
            Image.configure({
              inline: true,
              allowBase64: true,
            }),
            Link.configure({
              openOnClick: false,
              HTMLAttributes: {
                target: '_blank',
                rel: 'noopener noreferrer',
              },
            }),
            TextAlign.configure({
              types: ['heading', 'paragraph'],
            }),
            TextStyle,
            Color,
            FontFamily,
            Highlight.configure({
              multicolor: true,
            }),
            Table.configure({
              resizable: true,
            }),
            TableRow,
            TableHeader,
            TableCell,
            Gapcursor,
          ],
          content: this.htmlContent || '<p>Start writing your newsletter here...</p>',
        });
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
  }

  toggleHtmlView(): void {
    if (!this.showHtml()) {
      // Switching to HTML view
      if (this.editor) {
        this.htmlContent = this.editor.getHTML();
        this.editor.destroy();
        this.editor = null;
      }
      this.showHtml.set(true);
    } else {
      // Switching back to visual view
      this.showHtml.set(false);
      setTimeout(() => {
        const editorElement = document.querySelector('.editor-content');
        if (editorElement) {
          this.editor = new Editor({
            element: editorElement as HTMLElement,
            extensions: [
              Document,
              this.Div,
              Paragraph.extend({
                addAttributes() {
                  return {
                    ...this.parent?.(),
                    style: {
                      default: null,
                      parseHTML: element => element.getAttribute('style'),
                      renderHTML: attributes => {
                        if (!attributes['style']) {
                          return {};
                        }
                        return { style: attributes['style'] };
                      },
                    },
                  };
                },
              }),
              Text,
              Bold,
              Italic,
              Strike,
              Underline,
              Heading.extend({
                addAttributes() {
                  return {
                    ...this.parent?.(),
                    style: {
                      default: null,
                      parseHTML: element => element.getAttribute('style'),
                      renderHTML: attributes => {
                        if (!attributes['style']) {
                          return {};
                        }
                        return { style: attributes['style'] };
                      },
                    },
                  };
                },
              }).configure({
                levels: [1, 2, 3, 4, 5, 6],
              }),
              BulletList,
              OrderedList,
              ListItem,
              Blockquote.extend({
                addAttributes() {
                  return {
                    ...this.parent?.(),
                    style: {
                      default: null,
                      parseHTML: element => element.getAttribute('style'),
                      renderHTML: attributes => {
                        if (!attributes['style']) {
                          return {};
                        }
                        return { style: attributes['style'] };
                      },
                    },
                  };
                },
              }),
              HardBreak,
              Image.configure({
                inline: true,
                allowBase64: true,
              }),
              Link.configure({
                openOnClick: false,
                HTMLAttributes: {
                  target: '_blank',
                  rel: 'noopener noreferrer',
                },
              }),
              TextAlign.configure({
                types: ['heading', 'paragraph'],
              }),
              TextStyle,
              Color,
              FontFamily,
              Highlight.configure({
                multicolor: true,
              }),
              Table.configure({
                resizable: true,
              }),
              TableRow,
              TableHeader,
              TableCell,
              Gapcursor,
            ],
            content: this.htmlContent,
          });
        }
      }, 0);
    }
  }

  onHtmlChange(html: string): void {
    this.htmlContent = html;
  }

  triggerImageUpload(): void {
    this.fileInput.nativeElement.click();
  }

  onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (file && this.editor) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const base64 = e.target?.result as string;
        this.editor?.chain().focus().setImage({ src: base64 }).run();
      };
      reader.readAsDataURL(file);
    }

    // Reset input so the same file can be selected again
    input.value = '';
  }

  addImageUrl(): void {
    const url = prompt('Enter image URL:');
    if (url && this.editor) {
      this.editor.chain().focus().setImage({ src: url }).run();
    }
  }

  addLink(): void {
    const previousUrl = this.editor?.getAttributes('link')['href'];
    const url = prompt('Enter URL:', previousUrl);

    if (url === null) {
      return;
    }

    if (url === '') {
      this.editor?.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }

    this.editor?.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
  }

  save(): void {
    if (!this.title.trim()) {
      alert('Please enter a title');
      return;
    }

    const content = this.showHtml() ? this.htmlContent : (this.editor?.getHTML() || '');

    if (this.isNewMode) {
      this.newsletterService.createNewsletter(this.title, content);
      this.router.navigate(['/newsletter']);
    } else if (this.newsletterId) {
      this.newsletterService.updateNewsletter(this.newsletterId, this.title, content);
      this.router.navigate(['/newsletter']);
    }
  }

  goBack(): void {
    this.router.navigate(['/newsletter']);
  }

  // Toolbar action methods
  toggleBold(): void {
    this.editor?.chain().focus().toggleBold().run();
  }

  toggleItalic(): void {
    this.editor?.chain().focus().toggleItalic().run();
  }

  toggleUnderline(): void {
    this.editor?.chain().focus().toggleUnderline().run();
  }

  toggleStrike(): void {
    this.editor?.chain().focus().toggleStrike().run();
  }

  setHeading(level: 1 | 2 | 3): void {
    console.log('setHeading called', level, 'editor exists:', !!this.editor);
    if (!this.editor) return;
    const result = this.editor.chain().focus().toggleHeading({ level }).run();
    console.log('setHeading result:', result);
    console.log('current content:', this.editor.getHTML());
  }

  setParagraph(): void {
    console.log('setParagraph called');
    this.editor?.chain().focus().setParagraph().run();
  }

  setAlign(alignment: 'left' | 'center' | 'right'): void {
    this.editor?.chain().focus().setTextAlign(alignment).run();
  }

  toggleBulletList(): void {
    console.log('toggleBulletList called', 'editor exists:', !!this.editor);
    if (!this.editor) return;
    const result = this.editor.chain().focus().toggleBulletList().run();
    console.log('toggleBulletList result:', result);
    console.log('current content:', this.editor.getHTML());
  }

  toggleOrderedList(): void {
    console.log('toggleOrderedList called', 'editor exists:', !!this.editor);
    if (!this.editor) return;
    const result = this.editor.chain().focus().toggleOrderedList().run();
    console.log('toggleOrderedList result:', result);
  }

  toggleBlockquote(): void {
    console.log('toggleBlockquote called', 'editor exists:', !!this.editor);
    if (!this.editor) return;
    const result = this.editor.chain().focus().toggleBlockquote().run();
    console.log('toggleBlockquote result:', result);
    console.log('current content:', this.editor.getHTML());
  }

  isActive(name: string | Record<string, any>, attrs?: any): boolean {
    if (typeof name === 'string') {
      return this.editor?.isActive(name, attrs) || false;
    }
    return this.editor?.isActive(name) || false;
  }

  setTextColor(color: string): void {
    this.editor?.chain().focus().setColor(color).run();
    this.showColorPicker.set(false);
  }

  setHighlight(color: string): void {
    this.editor?.chain().focus().toggleHighlight({ color }).run();
    this.showHighlightPicker.set(false);
  }

  setFontFamily(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const font = select.value;
    if (font) {
      this.editor?.chain().focus().setFontFamily(font).run();
    } else {
      this.editor?.chain().focus().unsetFontFamily().run();
    }
    select.value = '';
  }

  insertTable(): void {
    this.editor?.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
  }

  loadTemplate(templateId: string): void {
    const template = this.newsletterService.getTemplateById(templateId);
    if (template) {
      this.htmlContent = template.html;
      if (this.editor) {
        this.editor.commands.setContent(template.html);
      }
      this.showTemplates.set(false);
    }
  }

  openSaveTemplateDialog(): void {
    const name = prompt('Enter template name:');
    if (!name) return;

    const description = prompt('Enter template description:');
    if (!description) return;

    const content = this.showHtml() ? this.htmlContent : (this.editor?.getHTML() || '');
    this.newsletterService.saveAsTemplate(name, description, content);
    alert('Template saved successfully!');
  }

  togglePreview(): void {
    const wasPreview = this.showPreview();

    // If switching TO preview mode, save current content
    if (!wasPreview && this.editor) {
      this.htmlContent = this.editor.getHTML();
    }

    this.showPreview.update(v => !v);

    // If switching FROM preview to visual mode, reinitialize editor
    if (wasPreview && !this.showHtml()) {
      setTimeout(() => {
        const editorElement = document.querySelector('.editor-content');
        if (editorElement) {
          this.initializeEditor(editorElement as HTMLElement);
        }
      }, 0);
    }
  }

  getPreviewContent(): SafeHtml {
    const html = this.showHtml() ? this.htmlContent : (this.editor?.getHTML() || '');
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}

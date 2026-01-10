import {Component, ContentChild, EventEmitter, HostListener, input, Output, signal, TemplateRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

export interface ColumnDefinition<T> {
  key: string;
  headerKey: string; // translation key
  sortable: boolean;
  getValue?: (item: T) => any;
  template?: TemplateRef<any>;
}

export interface RowAction<T> {
  labelKey: string; // translation key
  onClick: (item: T) => void;
  isDisabled?: (item: T) => boolean;
  stopPropagation?: boolean;
}

export interface CollectionAction {
  labelKey: string; // translation key
  onClick: () => void;
  show?: boolean;
}

@Component({
  selector: 'gym-generic-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  template: `
    <div class="p-3 sm:p-5 max-w-7xl mx-auto">
      <!-- Header with title and collection actions -->
      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 mb-5">
        <h2 class="text-2xl sm:text-3xl font-bold text-gray-800">{{ titleKey() | translate }}</h2>
        @if (collectionActions().length > 0) {
          <div class="flex gap-2 w-full sm:w-auto">
            @for (action of collectionActions(); track action.labelKey) {
              @if (action.show !== false) {
                <button
                  (click)="action.onClick()"
                  class="flex-1 sm:flex-none px-4 py-2 bg-blue-500 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-blue-600 transition-colors"
                >
                  {{ action.labelKey | translate }}
                </button>
              }
            }
          </div>
        }
      </div>

      <!-- Search bar -->
      @if (!isLoading() && !errorMessage()) {
        <div class="mb-5">
          <input
            type="text"
            [ngModel]="searchTerm()"
            (ngModelChange)="onSearchChange($event)"
            [placeholder]="searchPlaceholderKey() | translate"
            class="w-full px-3 py-2 text-base border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      }

      <!-- Loading state -->
      @if (isLoading()) {
        <div class="text-center py-10 text-gray-500 text-lg">
          {{ loadingKey() || 'common.loading' | translate }}
        </div>
      }

      <!-- Error state -->
      @if (errorMessage() && !isLoading()) {
        <div class="p-4 mb-4 bg-red-100 border border-red-400 text-red-700 rounded">
          {{ errorMessage() }}
        </div>
      }

      <!-- Content -->
      @if (!isLoading() && !errorMessage()) {
        @if (items().length === 0) {
          <!-- Empty state -->
          <div class="text-center py-20">
            <p class="text-gray-600 text-lg mb-4">{{ noItemsFoundKey()| translate }}</p>
            @if (createFirstItemKey() && collectionActions().length > 0) {
              <button
                (click)="collectionActions()[0].onClick()"
                class="px-4 py-2 bg-blue-500 text-white rounded border-none cursor-pointer text-sm font-medium hover:bg-blue-600 transition-colors"
              >
                {{ createFirstItemKey() || '' | translate }}
              </button>
            }
          </div>
        } @else {
          <!-- Desktop Table View -->
          <div class="hidden md:block overflow-x-auto bg-white shadow-md rounded-lg" style="overflow: visible;">
            <table class="w-full border-collapse" style="overflow: visible;">
              <thead class="bg-gray-100">
              <tr>
                @for (column of columns(); track column.key) {
                  <th
                    [class.cursor-pointer]="column.sortable"
                    [class.select-none]="column.sortable"
                    [class.hover:bg-gray-200]="column.sortable"
                    (click)="column.sortable ? sortBy(column.key) : null"
                    class="px-3 py-3 text-left border-b-2 border-gray-300 font-semibold transition-colors"
                  >
                    {{ column.headerKey | translate }}
                    @if (column.sortable) {
                      {{ getSortIcon(column.key) }}
                    }
                  </th>
                }
                @if (rowActions().length > 0) {
                  <th class="px-3 py-3 text-left border-b-2 border-gray-300 font-semibold">
                    {{ 'common.actions' | translate }}
                  </th>
                }
              </tr>
              </thead>
              <tbody>
                @for (item of items(); track trackByFn()($index, item)) {
                  <tr
                    [class.cursor-pointer]="onRowClick()"
                    [class.hover:bg-gray-50]="true"
                    class="transition-colors"
                    (click)="onRowClick()? handleRowClick(item) : null"
                  >
                    @for (column of columns(); track column.key) {
                      <td class="px-3 py-3 border-b border-gray-200">
                        @if (column.template) {
                          <ng-container
                            *ngTemplateOutlet="column.template; context: { $implicit: item }"></ng-container>
                        } @else {
                          {{ getCellValue(item, column) }}
                        }
                      </td>
                    }
                    @if (rowActions().length > 0) {
                      <td class="px-3 py-3 border-b border-gray-200 dropdown-container">
                        <div class="relative">
                          <button
                            (click)="toggleDropdown(trackByFn()($index, item), $event)"
                            class="px-2 py-1 text-gray-600 hover:bg-gray-200 rounded border-none cursor-pointer text-lg"
                            aria-label="Actions"
                          >
                            ⋮
                          </button>
                          @if (isDropdownOpen(trackByFn()($index, item))) {
                            <div
                              class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 overflow-hidden"
                              style="z-index: 9999;">
                              @for (action of rowActions(); track action.labelKey) {
                                <button
                                  (click)="executeAction(action, item, $event)"
                                  [disabled]="action.isDisabled ? action.isDisabled(item) : false"
                                  class="w-full text-left px-4 py-2 text-sm bg-white border-b border-gray-200 last:border-b-0 disabled:text-gray-400 disabled:cursor-not-allowed disabled:bg-gray-50 enabled:text-gray-700 enabled:hover:bg-gray-100 enabled:cursor-pointer block"
                                >
                                  {{ action.labelKey | translate }}
                                </button>
                              }
                            </div>
                          }
                        </div>
                      </td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Mobile Card View -->
          <div class="md:hidden space-y-3">
            @for (item of items(); track trackByFn()($index, item)) {
              <div
                [class.cursor-pointer]="onRowClick()"
                (click)="onRowClick() ? handleRowClick(item) : null"
                class="bg-white shadow rounded-lg p-4 hover:shadow-lg transition-shadow"
              >
                @for (column of columns(); track column.key) {
                  <div class="flex justify-between items-start py-2 border-b border-gray-100 last:border-b-0">
                    <span class="text-sm font-semibold text-gray-600 flex-shrink-0 mr-2">
                      {{ column.headerKey | translate }}:
                    </span>
                    <span class="text-sm text-gray-900 text-right">
                      @if (column.template) {
                        <ng-container
                          *ngTemplateOutlet="column.template; context: { $implicit: item }"></ng-container>
                      } @else {
                        {{ getCellValue(item, column) }}
                      }
                    </span>
                  </div>
                }
                @if (rowActions().length > 0) {
                  <div class="mt-3 pt-3 border-t border-gray-200">
                    <div class="relative">
                      <button
                        (click)="toggleDropdown(trackByFn()($index, item), $event)"
                        class="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 text-sm font-medium"
                      >
                        {{ 'common.actions' | translate }}
                      </button>
                      @if (isDropdownOpen(trackByFn()($index, item))) {
                        <div
                          class="absolute left-0 right-0 mt-2 bg-white rounded-lg shadow-lg border border-gray-200 overflow-hidden"
                          style="z-index: 9999;">
                          @for (action of rowActions(); track action.labelKey) {
                            <button
                              (click)="executeAction(action, item, $event)"
                              [disabled]="action.isDisabled ? action.isDisabled(item) : false"
                              class="w-full text-left px-4 py-2 text-sm bg-white border-b border-gray-200 last:border-b-0 disabled:text-gray-400 disabled:cursor-not-allowed disabled:bg-gray-50 enabled:text-gray-700 enabled:hover:bg-gray-100 enabled:cursor-pointer block"
                            >
                              {{ action.labelKey | translate }}
                            </button>
                          }
                        </div>
                      }
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        }
      }
    </div>
  `
})
export class GenericList<T> {
  titleKey = input.required<string>();
  searchPlaceholderKey = input.required<string>();
  noItemsFoundKey = input.required<string>();
  loadingKey = input<string | undefined>(undefined);
  createFirstItemKey = input<string | undefined>(undefined);

  items = input<T[]>([]);
  columns = input<ColumnDefinition<T>[]>([]);
  rowActions = input<RowAction<T>[]>([]);
  collectionActions = input<CollectionAction[]>([]);

  isLoading = input<boolean>(false);
  errorMessage = input<string | null>(null);

  searchTerm = input<string>('');
  sortColumn = input<string | null>(null);
  sortDirection = input<'asc' | 'desc'>('asc');

  trackByFn = input.required<(index: number, item: T) => any>();
  onRowClick = input<((item: T) => void) | undefined>(undefined);

  @Output() searchTermChange = new EventEmitter<string>();
  @Output() sortChange = new EventEmitter<{ column: string, direction: 'asc' | 'desc' }>();

  @ContentChild('rowTemplate') rowTemplate?: TemplateRef<any>;
  @ContentChild('cellTemplate') cellTemplate?: TemplateRef<any>;

  openDropdownId = signal<any>(null);

  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeDropdown();
  }

  onSearchChange(term: string): void {
    this.searchTermChange.emit(term);
  }

  sortBy(columnKey: string): void {
    const column = this.columns().find(c => c.key === columnKey);
    if (!column?.sortable) return;

    const currentSortColumn = this.sortColumn();
    const currentSortDirection = this.sortDirection();
    let nextDirection: 'asc' | 'desc' = 'asc';

    if (currentSortColumn === columnKey) {
      nextDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
    }

    this.sortChange.emit({ column: columnKey, direction: nextDirection });
  }

  getSortIcon(columnKey: string): string {
    if (this.sortColumn() !== columnKey) return '↕';
    return this.sortDirection() === 'asc' ? '↑' : '↓';
  }

  toggleDropdown(itemId: any, event: Event): void {
    event.stopPropagation();
    this.openDropdownId.set(this.openDropdownId() === itemId ? null : itemId);
  }

  closeDropdown(): void {
    this.openDropdownId.set(null);
  }

  isDropdownOpen(itemId: any): boolean {
    return this.openDropdownId() === itemId;
  }

  handleRowClick(item: T): void {
    const handler = this.onRowClick();
    if (handler) {
      handler(item);
    }
  }

  executeAction(action: RowAction<T>, item: T, event: Event): void {
    if (action.stopPropagation) {
      event.stopPropagation();
    }
    action.onClick(item);
    this.closeDropdown();
  }

  getCellValue(item: T, column: ColumnDefinition<T>): any {
    if (column.getValue) {
      return column.getValue(item);
    }
    return (item as any)[column.key];
  }
}

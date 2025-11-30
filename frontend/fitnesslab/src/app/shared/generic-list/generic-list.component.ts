import {Component, ContentChild, EventEmitter, HostListener, Input, Output, signal, TemplateRef} from '@angular/core';
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
  selector: 'app-generic-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './generic-list.component.html'
})
export class GenericListComponent<T> {
  @Input() titleKey!: string;
  @Input() searchPlaceholderKey!: string;
  @Input() noItemsFoundKey!: string;
  @Input() loadingKey?: string;
  @Input() createFirstItemKey?: string;

  @Input() items: T[] = [];
  @Input() columns: ColumnDefinition<T>[] = [];
  @Input() rowActions: RowAction<T>[] = [];
  @Input() collectionActions: CollectionAction[] = [];

  @Input() isLoading = false;
  @Input() errorMessage: string | null = null;

  @Input() searchTerm = '';
  @Input() sortColumn: string | null = null;
  @Input() sortDirection: 'asc' | 'desc' = 'asc';

  @Input() trackByFn!: (index: number, item: T) => any;
  @Input() onRowClick?: (item: T) => void;

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
    const column = this.columns.find(c => c.key === columnKey);
    if (!column?.sortable) return;

    if (this.sortColumn === columnKey) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = columnKey;
      this.sortDirection = 'asc';
    }

    this.sortChange.emit({ column: columnKey, direction: this.sortDirection });
  }

  getSortIcon(columnKey: string): string {
    if (this.sortColumn !== columnKey) return '↕';
    return this.sortDirection === 'asc' ? '↑' : '↓';
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

  handleRowClick(item: T, event: Event): void {
    if (this.onRowClick) {
      this.onRowClick(item);
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

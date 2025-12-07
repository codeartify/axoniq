import {
  Component,
  ContentChild,
  EventEmitter,
  HostListener,
  Input,
  Output,
  signal,
  TemplateRef,
  input
} from '@angular/core';
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

  handleRowClick(item: T, event: Event): void {
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

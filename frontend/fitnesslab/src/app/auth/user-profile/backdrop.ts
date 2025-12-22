import {Component, output} from '@angular/core';

@Component({
  selector: 'gym-backdrop',
  template: `
    <div
      (click)="closeMenu()"
      (keydown.enter)="closeMenu()"
      (keydown.escape)="closeMenu()"
      tabindex="0"
      role="button"
      aria-label="Close menu"
      class="fixed inset-0 z-40">
    </div>
  `,
})
export class Backdrop {
  menuClosed = output<void>();

  closeMenu(): void {
    this.menuClosed.emit();
  }

}

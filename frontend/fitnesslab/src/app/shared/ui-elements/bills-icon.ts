import {Component} from '@angular/core';

@Component({
  selector: 'gym-bills-icon',
  template: `
    <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
      <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
      </svg>
    </div>
  `,
})
export class BillsIcon {

}

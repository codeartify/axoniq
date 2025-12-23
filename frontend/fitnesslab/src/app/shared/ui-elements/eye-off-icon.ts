import {Component} from '@angular/core';

@Component({
  selector: 'gym-eye-off-icon',
  template: `
    <svg xmlns="http://www.w3.org/2000/svg"
         viewBox="0 0 24 24"
         width="1em" height="1em"
         fill="none" stroke="currentColor"
         stroke-width="2"
         stroke-linecap="round"
         stroke-linejoin="round"
         aria-hidden="true"
         style="vertical-align:-0.125em">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z"/>
      <circle cx="12" cy="12" r="3"/>
      <path d="M3 3l18 18"/>
    </svg>
  `
})
export class EyeOffIcon {
}

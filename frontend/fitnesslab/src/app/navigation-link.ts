import {Component, input} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';


export interface NavigationLinkConfiguration {
  label: string;
  route: string;
}

@Component({
  selector: 'gym-navigation-link',
  imports: [
    RouterLink,
    RouterLinkActive,
    TranslatePipe
  ],
  template: `
    @let config = navigationLinkConfiguration();

    <a [routerLink]="config.route"
       routerLinkActive="text-blue-400 font-semibold"
       [routerLinkActiveOptions]="{exact: false}"
       [class]="mobile() ? 'block py-2 px-4 text-slate-300 hover:bg-slate-700 hover:text-blue-400 rounded transition-colors' : 'text-slate-300 hover:text-blue-400 transition-colors'">
      {{ config.label | translate }}
    </a>
  `,
})
export class NavigationLink {
  navigationLinkConfiguration = input.required<NavigationLinkConfiguration>();
  mobile = input<boolean>(false);
}

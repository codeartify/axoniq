import {Component, input, signal} from '@angular/core';
import {NavigationLink, NavigationLinkConfiguration} from './navigation-link';

@Component({
  selector: 'gym-navigation',
  imports: [
    NavigationLink
  ],
  template: `
    <nav [class]="mobile() ? 'flex flex-col gap-2' : 'flex gap-6'">
      @for (configuration of navigationLinkConfigs(); track configuration) {
        <gym-navigation-link [navigationLinkConfiguration]="configuration" [mobile]="mobile()"/>
      }
    </nav>
  `,
})
export class Navigation {
  mobile = input<boolean>(false);

  navigationLinkConfigs = signal<NavigationLinkConfiguration[]>([
    {label: 'nav.dashboard', route: '/dashboard'},
    {label: 'nav.customers', route: '/customers'},
    {label: 'nav.products', route: '/products'},
    {label: 'nav.invoices', route: '/invoices'},
  ]);
}

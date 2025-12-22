import {Component, signal} from '@angular/core';
import {NavigationLink, NavigationLinkConfiguration} from './navigation-link';

@Component({
  selector: 'gym-navigation',
  imports: [
    NavigationLink
  ],
  template: `
    <nav class="flex gap-6">
      @for (configuration of navigationLinkConfigs(); track configuration) {
        <gym-navigation-link [navigationLinkConfiguration]="configuration"/>
      }
    </nav>
  `,
})
export class Navigation {
  navigationLinkConfigs = signal<NavigationLinkConfiguration[]>([
    {label: 'nav.customers', route: '/customers'},
    {label: 'nav.products', route: '/products'},
    {label: 'nav.invoices', route: '/invoices'},
  ]);
}

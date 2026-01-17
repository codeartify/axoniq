import {Component, computed, inject, input, signal} from '@angular/core';
import {NavigationLink, NavigationLinkConfiguration} from './navigation-link';
import AuthService from './auth/auth.service';

@Component({
  selector: 'gym-navigation',
  imports: [
    NavigationLink
  ],
  template: `
    <nav [class]="mobile() ? 'flex flex-col gap-2' : 'flex gap-6'">
      @for (configuration of visibleNavigationLinks(); track configuration) {
        <gym-navigation-link [navigationLinkConfiguration]="configuration" [mobile]="mobile()"/>
      }
    </nav>
  `,
})
export class Navigation {
  private authService = inject(AuthService);
  mobile = input<boolean>(false);

  navigationLinkConfigs = signal<NavigationLinkConfiguration[]>([
    {label: 'nav.dashboard', route: '/dashboard'},
    {label: 'nav.customers', route: '/customers'},
    {label: 'nav.products', route: '/products'},
    {label: 'nav.invoices', route: '/invoices'},
    {label: 'nav.newsletter', route: '/newsletter'},
  ]);

  visibleNavigationLinks = computed(() => {
    //const isAdmin = this.authService.hasRole('admin')();
    return this.navigationLinkConfigs().filter(config => {
      // Only show newsletter to admin users
      if (config.route === '/newsletter') {
        return true;
      }
      return true;
    });
  });
}

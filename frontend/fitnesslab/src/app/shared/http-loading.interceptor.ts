import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { LoadingService } from './ui-elements/loading.service';

export const httpLoadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);

  // Only show loading for POST, PUT, DELETE requests
  if (req.method === 'POST' || req.method === 'PUT' || req.method === 'DELETE') {
    loadingService.show();

    return next(req).pipe(
      finalize(() => {
        loadingService.hide();
      })
    );
  }

  return next(req);
};

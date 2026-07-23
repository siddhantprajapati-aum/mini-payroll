import { HttpErrorResponse } from '@angular/common/http';

export function extractApiError(error: unknown, fallback = 'Something went wrong.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 0) {
    return 'Cannot reach the server. Make sure the backend is running on port 8080.';
  }

  const body = error.error;
  if (typeof body === 'string' && body.trim()) {
    return body;
  }

  if (body && typeof body === 'object') {
    if (typeof body.message === 'string' && body.message.trim()) {
      return body.message;
    }
    if (body.messages && typeof body.messages === 'object') {
      return Object.values(body.messages).join('; ');
    }
    if (Array.isArray(body.errors) && body.errors.length) {
      return body.errors
        .map((e: { defaultMessage?: string; message?: string }) =>
          e.defaultMessage || e.message || 'Validation error'
        )
        .join('; ');
    }
    if (body.errors && typeof body.errors === 'object') {
      return Object.values(body.errors).flat().join('; ');
    }
  }

  if (error.status >= 500) {
    return 'Server error. Please try again.';
  }

  return error.message || fallback;
}

export const environment = {
  production: true,
  // Same-origin when UI is served by Spring Boot (Docker / Render single URL).
  // If frontend is hosted separately, change to full backend URL, e.g.:
  // 'https://YOUR-SERVICE.onrender.com/api/v1'
  apiBaseUrl: '/api/v1'
};

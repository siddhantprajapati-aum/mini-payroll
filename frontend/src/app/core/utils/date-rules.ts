export function toLocalDateString(date = new Date()): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function parseLocalDate(value: string): Date {
  return new Date(`${value}T00:00:00`);
}

export function isWeekend(value: string): boolean {
  const day = parseLocalDate(value).getDay();
  return day === 0 || day === 6;
}

export function isFutureDate(value: string, today = toLocalDateString()): boolean {
  return value > today;
}

export function isPastDate(value: string, today = toLocalDateString()): boolean {
  return value < today;
}

export function nearestPastOrTodayWeekday(from = new Date()): string {
  const cursor = new Date(from.getFullYear(), from.getMonth(), from.getDate());
  while (cursor.getDay() === 0 || cursor.getDay() === 6) {
    cursor.setDate(cursor.getDate() - 1);
  }
  return toLocalDateString(cursor);
}

export function nearestFutureOrTodayWeekday(from = new Date()): string {
  const cursor = new Date(from.getFullYear(), from.getMonth(), from.getDate());
  while (cursor.getDay() === 0 || cursor.getDay() === 6) {
    cursor.setDate(cursor.getDate() + 1);
  }
  return toLocalDateString(cursor);
}

export function eachDateInclusive(startDate: string, endDate: string): string[] {
  const dates: string[] = [];
  const cursor = parseLocalDate(startDate);
  const end = parseLocalDate(endDate);

  while (cursor <= end) {
    dates.push(toLocalDateString(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }

  return dates;
}

export function eachWeekdayInclusive(startDate: string, endDate: string): string[] {
  return eachDateInclusive(startDate, endDate).filter((date) => !isWeekend(date));
}

export function dayOfWeekLabel(value: string): string {
  return parseLocalDate(value).toLocaleDateString(undefined, { weekday: 'long' });
}

export function allowsWeekendWork(salaryType: string | null | undefined): boolean {
  return salaryType === 'DAILY';
}


// from https://stackoverflow.com/a/69122877

export function timeAgo(input: Date | string): string {
  const date = (input instanceof Date) ? input : new Date(input);
  const formatter = new Intl.RelativeTimeFormat('en');
  const ranges: { [unit in Intl.RelativeTimeFormatUnit]?: number } = {
    years: 3600 * 24 * 365,
    months: 3600 * 24 * 30,
    weeks: 3600 * 24 * 7,
    days: 3600 * 24,
    hours: 3600,
    minutes: 60,
    seconds: 1
  };
  const secondsElapsed = (date.getTime() - Date.now()) / 1000;
  for (let key in ranges) {
    if (ranges[key] < Math.abs(secondsElapsed)) {
      const delta = secondsElapsed / ranges[key];
      let foo: string;
      return formatter.format(Math.round(delta), key as Intl.RelativeTimeFormatUnit);
    }
  }
}


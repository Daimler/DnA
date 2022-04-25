import { Envs } from '../globals/Envs';
import { IUserInfo } from '../globals/types';

declare global {
  interface Window {
    _paq?: any;
  }
}

const baseUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:3000/api`;

export const createQueryParams = (obj: any): string => {
  const str = [];
  for (const p in obj) {
    if (obj.hasOwnProperty(p)) {
      str.push(p + '=' + obj[p]);
    }
  }
  return str.join('&');
};

export const getImageUrlById = (imageId: string): string => {
  if (!imageId) {
    return '';
  }

  return `${baseUrl}/images/${imageId}`;
};

export const getFullNameOfUser = (user: IUserInfo) => {
  if (!user) {
    return '';
  }

  if (user.firstName && user.lastName) {
    return `${user.lastName}, ${user.firstName}`;
  }

  if (user.firstName || user.lastName) {
    return `${user.firstName || user.lastName}`;
  } else {
    if (user.id) {
      return user.id;
    }
    return '';
  }
};
// Removing uery parameter from an current URL code and state params are hanled here //
export const removeURLParameter = (url: string, paramKey: string) => {
  const r = new URL(url);
  r.searchParams.delete(paramKey);
  return r.href;
};

// Format big number value to as K, M, B for display
export const DataFormater = (value: number) => {
  const sign = Math.sign(Number(value));
  // Nine Zeroes for Billions
  let formatedValue = (
    Math.abs(Number(value)) >= 1.0e9
      ? (sign * (Math.abs(Number(value)) / 1.0e9)).toFixed(2) + 'B'
      : // Six Zeroes for Millions
      Math.abs(Number(value)) >= 1.0e6
      ? (sign * (Math.abs(Number(value)) / 1.0e6)).toFixed(2) + 'M'
      : // Three Zeroes for Thousands
      Math.abs(Number(value)) >= 1.0e3
      ? (sign * (Math.abs(Number(value)) / 1.0e3)).toFixed(2) + 'K'
      : Math.abs(Number(value)).toFixed(2)
  ).replace('.00', '');

  if (formatedValue.includes('.')) {
    formatedValue = formatedValue.replace('0B', 'B').replace('0M', 'M').replace('0K', 'K');
  }

  return formatedValue;
};

export const attachEllipsis = (value: string, length: number) => {
  return value?.length >= length ? value.substring(0, length) + '...' : value;
};

export const trackPageView = (url: string, title: string, userId: string) => {
  // For tracking page view in matamo
  if (window._paq) {
    window._paq.push(['setCustomUrl', '/hash' + url]);
    window._paq.push(['setDocumentTitle', title]);
    window._paq.push(['setUserId', userId]);
    window._paq.push(['trackPageView']);
    window._paq.push(['enableLinkTracking']);
  }
};

export const trackEvent = (category: string, action: string, name: string, value?: string) => {
  // For tracking event in matamo
  if (window._paq) {
    const eventArr = ['trackEvent', category, action, name];
    if (value) {
      eventArr.push(value);
    }
    window._paq.push(eventArr);
  }
};

export const getDateFromTimestamp = (givenDate: string, seperator?: string) => {
  const d = new Date(givenDate);
  const sep = seperator || '-';
  return d.getUTCDate() + sep + (d.getUTCMonth() + 1) + sep + d.getUTCFullYear();
};

export const getDateTimeFromTimestamp = (givenDate: string, seperator?: string) => {
  const d = new Date(givenDate);
  const time = d.getUTCHours();
  const mins = d.getUTCMinutes();
  return (
    getDateFromTimestamp(givenDate, seperator) +
    ' at ' +
    (time < 10 ? '0' : '') +
    time +
    ':' +
    (mins < 10 ? '0' : '') +
    mins
  );
};

export const getDateDifferenceFromToday = (dateFrom: string) => {
  const dateSplitted = dateFrom.split('-');
  // Making format in MM-DD-YYYY;
  const tempDate = dateSplitted[1] + '-' + dateSplitted[0] + '-' + dateSplitted[2];
  const date1 = new Date(tempDate);
  const date2 = new Date();
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  // const diffTime = Math.abs(date2.getDate() - date1.getDate());
  const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
  return diffDays;
};

export const getDateDifferenceFromTodayUsingGetDate = (dateFrom: string) => {
  const dateSplitted = dateFrom.split('-');
  // Making format in MM-DD-YYYY;
  const tempDate = dateSplitted[1] + '-' + dateSplitted[0] + '-' + dateSplitted[2];
  const date1 = new Date(tempDate);
  const date2 = new Date();
  const diffTime = Math.abs(date2.getDate() - date1.getDate());
  const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
  return diffDays;
};

export const getDateAfterSomeDays = (noOfDays: number) => {
  const someDate = new Date();
  const numberOfDaysToAdd = noOfDays;
  someDate.setDate(someDate.getDate() + numberOfDaysToAdd);
  const dd = someDate.getDate();
  const mm = someDate.getMonth() + 1;
  const y = someDate.getFullYear();

  const someFormattedDate = dd + '-' + mm + '-' + y;
  return someFormattedDate;
};

export const convertTextToLink = (text: string, env: string) => {
  if (text.lastIndexOf('[') === -1 && text.lastIndexOf(']') === -1) {
    return text;
  }
  const tempArray = [{ data: '', link: '' }];
  const a = '](';
  let linkData = '';
  let getLinkFromString = '';
  text.split(a).forEach((item) => {
    if (item.includes('[')) {
      if (item.includes('[') && item.includes('/)')) {
        tempArray.push({ data: item.split('[')[1], link: item.split('/)')[0] });
      } else {
        linkData = item.split('[')[1];
      }
    } else if (item.includes('/)')) {
      getLinkFromString = item.split('/)')[0];
      tempArray.push({ data: linkData, link: getLinkFromString });
    }
  });
  tempArray.forEach((item) => {
    const searchString = '[' + item.data + '](' + item.link + '/)';
    const formattedURL =
      (env === 'production' ? Envs.DATAIKU_LIVE_APP_URL : Envs.DATAIKU_TRAINING_APP_URL) + '/' + item.link;
    const anchor = `<a href=${formattedURL} target="_blank">${item.data}</a>`;
    text = text.replace(searchString, anchor);
  });
  return text;
};

export const getDivisionsQueryValue = (divisions: string[], subDivisions: string[]) => {
  let divisionIds = divisions.join(',');
  if (divisions.length > 0) {
    const distinctSelectedDivisions = divisions;
    const tempArr: any[] = [];
    distinctSelectedDivisions.forEach((item) => {
      const tempString = '{' + item + ',[]}';
      tempArr.push(tempString);
    });
    divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
  }

  if (subDivisions.length > 0) {
    const distinctSelectedDivisions = divisions;
    const tempArr: any[] = [];
    let hasEmpty = false; // To find none selected in sub division since its not mandatory
    const emptySubDivId = 'EMPTY';
    distinctSelectedDivisions.forEach((item) => {
      const tempSubdiv = subDivisions.map((value) => {
        const tempArray = value.split('-');
        const subDivId = tempArray[0];
        if (subDivId === emptySubDivId) {
          hasEmpty = true;
        }
        if (item === tempArray[1]) {
          return subDivId;
        }
      });

      if (hasEmpty && !tempSubdiv.includes(emptySubDivId)) {
        tempSubdiv.unshift(emptySubDivId);
      }

      let tempString = '';

      if (tempSubdiv.length === 0) {
        tempString += '{' + item + ',[]}';
      } else {
        tempString += '{' + item + ',[' + tempSubdiv.filter((div) => div) + ']}';
      }

      tempArr.push(tempString);
    });
    divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
  }
  if (divisions.length === 0) {
    divisionIds = '';
  }
  return divisionIds;
};

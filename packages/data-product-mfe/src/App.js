import React, { useEffect } from 'react';

import { Provider } from 'react-redux';
import store, { history } from './store';

import Routes, { protectedRoutes } from './components/DataRoutes';

const App = ({ user, ...rest }) => {
  useEffect(() => {
    const pathName = rest.history?.location?.pathname?.replace('/data', '') || '/';
    const isValidRoute = protectedRoutes.find((route) => pathName === route.path)?.path;

    if (user?.roles?.length > 0 && rest.history?.location?.pathname.includes('/data')) {
      if (isValidRoute) {
        history.replace(isValidRoute);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
};

export default App;

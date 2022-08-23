import React, { useEffect } from 'react';

import { Provider } from 'react-redux';
import store, { history } from './store';

import Routes from './components/DataProductRoutes';

const App = ({ user, ...rest }) => {
  useEffect(() => {
    user?.roles?.length > 0 && rest.history?.location?.pathname === '/dataproduct' && history.replace('/');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
};

export default App;

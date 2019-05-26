import React from 'react';
import { Switch } from 'react-router-dom';

// tslint:disable-next-line:no-unused-variable
import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Item from './item';
import Cart from './cart';
import Order from './order';
/* jhipster-needle-add-route-import - JHipster will add routes here */

const Routes = ({ match }) => (
  <div>
    <Switch>
      {/* prettier-ignore */}
      <ErrorBoundaryRoute path={`${match.url}/item`} component={Item} />
      <ErrorBoundaryRoute path={`${match.url}/cart`} component={Cart} />
      <ErrorBoundaryRoute path={`${match.url}/order`} component={Order} />
      {/* jhipster-needle-add-route-path - JHipster will add routes here */}
    </Switch>
  </div>
);

export default Routes;

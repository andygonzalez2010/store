import React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
// tslint:disable-next-line:no-unused-variable
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, ICrudPutAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { ICart } from 'app/shared/model/cart.model';
import { getEntities as getCarts } from 'app/entities/cart/cart.reducer';
import { IItem } from 'app/shared/model/item.model';
import { getEntities as getItems } from 'app/entities/item/item.reducer';
import { getEntity, updateEntity, createEntity, reset } from './order.reducer';
import { IOrder } from 'app/shared/model/order.model';
// tslint:disable-next-line:no-unused-variable
import { convertDateTimeFromServer, convertDateTimeToServer } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IOrderUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export interface IOrderUpdateState {
  isNew: boolean;
  cartId: string;
  itemId: string;
}

export class OrderUpdate extends React.Component<IOrderUpdateProps, IOrderUpdateState> {
  constructor(props) {
    super(props);
    this.state = {
      cartId: '0',
      itemId: '0',
      isNew: !this.props.match.params || !this.props.match.params.id
    };
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextProps.updateSuccess !== this.props.updateSuccess && nextProps.updateSuccess) {
      this.handleClose();
    }
  }

  componentDidMount() {
    if (!this.state.isNew) {
      this.props.getEntity(this.props.match.params.id);
    }

    this.props.getCarts();
    this.props.getItems();
  }

  saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const { orderEntity } = this.props;
      const entity = {
        ...orderEntity,
        ...values
      };

      if (this.state.isNew) {
        this.props.createEntity(entity);
      } else {
        this.props.updateEntity(entity);
      }
    }
  };

  handleClose = () => {
    this.props.history.push('/entity/order');
  };

  render() {
    const { orderEntity, carts, items, loading, updating } = this.props;
    const { isNew } = this.state;

    return (
      <div>
        <Row className="justify-content-center">
          <Col md="8">
            <h2 id="storeApp.order.home.createOrEditLabel">
              <Translate contentKey="storeApp.order.home.createOrEditLabel">Create or edit a Order</Translate>
            </h2>
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col md="8">
            {loading ? (
              <p>Loading...</p>
            ) : (
              <AvForm model={isNew ? {} : orderEntity} onSubmit={this.saveEntity}>
                {!isNew ? (
                  <AvGroup>
                    <Label for="order-id">
                      <Translate contentKey="global.field.id">ID</Translate>
                    </Label>
                    <AvInput id="order-id" type="text" className="form-control" name="id" required readOnly />
                  </AvGroup>
                ) : null}
                <AvGroup>
                  <Label id="quantityLabel" for="order-quantity">
                    <Translate contentKey="storeApp.order.quantity">Quantity</Translate>
                  </Label>
                  <AvField
                    id="order-quantity"
                    type="string"
                    className="form-control"
                    name="quantity"
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') },
                      min: { value: 1, errorMessage: translate('entity.validation.min', { min: 1 }) },
                      number: { value: true, errorMessage: translate('entity.validation.number') }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <Label for="order-cart">
                    <Translate contentKey="storeApp.order.cart">Cart</Translate>
                  </Label>
                  <AvInput id="order-cart" type="select" className="form-control" name="cartId" required>
                    {carts
                      ? carts.map(otherEntity => (
                          <option value={otherEntity.id} key={otherEntity.id}>
                            {otherEntity.id}
                          </option>
                        ))
                      : null}
                  </AvInput>
                  <AvFeedback>
                    <Translate contentKey="entity.validation.required">This field is required.</Translate>
                  </AvFeedback>
                </AvGroup>
                <AvGroup>
                  <Label for="order-item">
                    <Translate contentKey="storeApp.order.item">Item</Translate>
                  </Label>
                  <AvInput id="order-item" type="select" className="form-control" name="itemId" required>
                    {items
                      ? items.map(otherEntity => (
                          <option value={otherEntity.id} key={otherEntity.id}>
                            {otherEntity.title}
                          </option>
                        ))
                      : null}
                  </AvInput>
                  <AvFeedback>
                    <Translate contentKey="entity.validation.required">This field is required.</Translate>
                  </AvFeedback>
                </AvGroup>
                <Button tag={Link} id="cancel-save" to="/entity/order" replace color="info">
                  <FontAwesomeIcon icon="arrow-left" />
                  &nbsp;
                  <span className="d-none d-md-inline">
                    <Translate contentKey="entity.action.back">Back</Translate>
                  </span>
                </Button>
                &nbsp;
                <Button color="primary" id="save-entity" type="submit" disabled={updating}>
                  <FontAwesomeIcon icon="save" />
                  &nbsp;
                  <Translate contentKey="entity.action.save">Save</Translate>
                </Button>
              </AvForm>
            )}
          </Col>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = (storeState: IRootState) => ({
  carts: storeState.cart.entities,
  items: storeState.item.entities,
  orderEntity: storeState.order.entity,
  loading: storeState.order.loading,
  updating: storeState.order.updating,
  updateSuccess: storeState.order.updateSuccess
});

const mapDispatchToProps = {
  getCarts,
  getItems,
  getEntity,
  updateEntity,
  createEntity,
  reset
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OrderUpdate);

import './home.scss';

import React from 'react';
import InfiniteScroll from 'react-infinite-scroller';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Translate, getSortState, IPaginationBaseState } from 'react-jhipster';
import { connect } from 'react-redux';
import {
  Row,
  Col,
  Alert,
  Button,
  Modal,
  Form,
  FormGroup,
  Label,
  Input,
  ModalBody,
  Card,
  CardImg,
  CardText,
  CardBody,
  CardTitle,
  CardSubtitle
} from 'reactstrap';

import { getSession } from 'app/shared/reducers/authentication';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { getEntities, reset } from 'app/entities/item/item.reducer';
import { IItem } from 'app/shared/model/item.model';
import { ICart } from 'app/shared/model/cart.model';
import { IOrder } from 'app/shared/model/order.model';
import { IRootState } from 'app/shared/reducers';
import { createEntity as createCartEntity } from 'app/entities/cart/cart.reducer';

export interface IItemProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export interface IHomeState extends IPaginationBaseState {
  modal: boolean;
  selected: IItem;
  _cart: ICart;
  _order: IOrder;
}

export type IItemState = IHomeState;

export class Home extends React.Component<IItemProps, IItemState, IRootState> {
  state: IItemState = {
    ...getSortState(this.props.location, ITEMS_PER_PAGE),
    modal: false,
    selected: null,
    _cart: { orders: [] },
    _order: null
  };

  constructor(props) {
    super(props);
    this.canAddToCart = this.canAddToCart.bind(this);
    this.addToCart = this.addToCart.bind(this);
    this.handleOrderQuantityChange = this.handleOrderQuantityChange.bind(this);
  }

  toggle = (item: IItem) => ev => {
    const { _cart } = this.state;
    let order = null;
    if (item) {
      order = _cart.orders.find(({ itemId }) => itemId === item.id);
      if (order == null) {
        order = {
          itemId: item.id,
          itemTitle: item.title,
          quantity: 0
        };
      }
    }
    this.setState(prevState => ({
      selected: item,
      _order: order,
      modal: !prevState.modal
    }));
  };

  handleOrderQuantityChange(event) {
    this.state._order.quantity = event.target.value;
    this.setState(() => ({
      _order: this.state._order
    }));
  }

  addToCart(event) {
    event.preventDefault();
    const { _cart, _order } = this.state;
    const orderInCart = _cart.orders.find(({ itemId }) => itemId === _order.itemId);
    if (orderInCart == null) {
      _cart.orders.push(_order);
    } else {
      orderInCart.quantity = _order.quantity;
    }
    this.toggle(null)(null);
  }

  canAddToCart(event) {
    const { _cart, _order, selected } = this.state;
    const orderInCart = _cart.orders.find(({ itemId }) => itemId === _order.itemId);
    const syncedQuantityWithCart = orderInCart ? orderInCart.quantity + _order.quantity : _order.quantity;
    return _order.quantity >= 0 && (selected && selected.count >= syncedQuantityWithCart);
  }

  async componentDidMount() {
    await this.props.getSession();
    this.reset();
  }

  componentDidUpdate() {
    if (this.props.updateSuccess) {
      this.reset();
    }
  }

  reset = () => {
    this.props.reset();
    this.setState({ activePage: 1 }, () => {
      this.getEntities();
    });
  };

  handleLoadMore = () => {
    if (window.pageYOffset > 0) {
      this.setState({ activePage: this.state.activePage + 1 }, () => this.getEntities());
    }
  };

  sort = prop => () => {
    this.setState(
      {
        order: this.state.order === 'asc' ? 'desc' : 'asc',
        sort: prop
      },
      () => {
        this.reset();
      }
    );
  };

  getEntities = () => {
    const { activePage, itemsPerPage, sort, order } = this.state;
    this.props.getEntities(activePage - 1, itemsPerPage, `${sort},${order}`);
  };

  render() {
    const { itemList, match, account } = this.props;
    return (
      <Row>
        <Col md="12">
          <h2>
            <Translate contentKey="home.title">Welcome, Java Hipster!</Translate>
          </h2>
          <p className="lead">
            <Translate contentKey="home.subtitle">This is your homepage</Translate>
          </p>
          {account && account.login ? (
            <div>
              <Alert color="success">
                <Translate contentKey="home.logged.message" interpolate={{ username: account.login }}>
                  You are logged in as user {account.login}.
                </Translate>
              </Alert>
            </div>
          ) : (
            <div>
              <Alert color="warning">
                <Translate contentKey="global.messages.info.authenticated.prefix">If you want to </Translate>
                <Link to="/login" className="alert-link">
                  <Translate contentKey="global.messages.info.authenticated.link"> sign in</Translate>
                </Link>
                <Translate contentKey="global.messages.info.authenticated.suffix">
                  , you can try the default accounts:
                  <br />- Administrator (login=&quot;admin&quot; and password=&quot;admin&quot;)
                  <br />- User (login=&quot;user&quot; and password=&quot;user&quot;).
                </Translate>
                <br />
                <Translate contentKey="global.messages.info.register.noaccount">You do not have an account yet?</Translate>&nbsp;
                <Link to="/register" className="alert-link">
                  <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
                </Link>
              </Alert>
            </div>
          )}
          <InfiniteScroll
            pageStart={this.state.activePage}
            loadMore={this.handleLoadMore}
            hasMore={this.state.activePage - 1 < this.props.links.next}
            loader={<div className="loader">Loading ...</div>}
            threshold={0}
            initialLoad={false}
          >
            <Row>
              {itemList.map((item, i) => (
                <Col lg="3" md="4" sm="6" key={`entity-${i}`}>
                  <Card onClick={this.toggle(item)}>
                    {item.image ? (
                      <CardImg top width="100%" src={`data:${item.imageContentType};base64,${item.image}`} alt={`${item.title} image`} />
                    ) : null}
                    <CardBody>
                      <CardTitle>
                        {item.title}
                        <small>{item.count}</small>
                      </CardTitle>
                      <CardSubtitle>{item.price}$</CardSubtitle>
                      <CardText>{item.description}</CardText>
                    </CardBody>
                  </Card>
                </Col>
              ))}
            </Row>
          </InfiniteScroll>
        </Col>
        <Modal isOpen={this.state.modal} toggle={this.toggle(null)}>
          <ModalBody>
            {this.state.selected && this.state._order ? (
              <Card>
                <CardBody>
                  <Row>
                    <Col md="6">
                      {this.state.selected ? (
                        <CardImg
                          width="100%"
                          src={`data:${this.state.selected.imageContentType};base64,${this.state.selected.image}`}
                          alt={`${this.state.selected.title} image`}
                        />
                      ) : null}
                    </Col>
                    <Col md="6">
                      <CardTitle>
                        {this.state.selected.title}
                        <small>{this.state.selected.count}</small>
                      </CardTitle>
                      <CardSubtitle>{this.state.selected.price}$</CardSubtitle>
                      <Form onSubmit={this.addToCart}>
                        <FormGroup>
                          <Label for="quantity">Quantity</Label>
                          <Input
                            value={this.state._order.quantity}
                            onChange={this.handleOrderQuantityChange}
                            min="0"
                            max={this.state.selected.count}
                            type="number"
                            name="quantity"
                          />
                        </FormGroup>
                        <h4>Description</h4>
                        <CardText>{this.state.selected.description}</CardText>
                        <Button>
                          <span>Add to cart</span>
                        </Button>
                      </Form>
                    </Col>
                  </Row>
                </CardBody>
              </Card>
            ) : null}
          </ModalBody>
        </Modal>
      </Row>
    );
  }
}

const mapStateToProps = storeState => ({
  account: storeState.authentication.account,
  isAuthenticated: storeState.authentication.isAuthenticated,
  itemList: storeState.item.entities,
  totalItems: storeState.item.totalItems,
  links: storeState.item.links,
  entity: storeState.item.entity,
  updateSuccess: storeState.item.updateSuccess
});

const mapDispatchToProps = {
  getSession,
  getEntities,
  reset
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Home);

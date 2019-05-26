import React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
// tslint:disable-next-line:no-unused-variable
import { Translate, ICrudGetAction, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './item.reducer';
import { IItem } from 'app/shared/model/item.model';
// tslint:disable-next-line:no-unused-variable
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IItemDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export class ItemDetail extends React.Component<IItemDetailProps> {
  componentDidMount() {
    this.props.getEntity(this.props.match.params.id);
  }

  render() {
    const { itemEntity } = this.props;
    return (
      <Row>
        <Col md="8">
          <h2>
            <Translate contentKey="storeApp.item.detail.title">Item</Translate> [<b>{itemEntity.id}</b>]
          </h2>
          <dl className="jh-entity-details">
            <dt>
              <span id="title">
                <Translate contentKey="storeApp.item.title">Title</Translate>
              </span>
            </dt>
            <dd>{itemEntity.title}</dd>
            <dt>
              <span id="description">
                <Translate contentKey="storeApp.item.description">Description</Translate>
              </span>
            </dt>
            <dd>{itemEntity.description}</dd>
            <dt>
              <span id="price">
                <Translate contentKey="storeApp.item.price">Price</Translate>
              </span>
            </dt>
            <dd>{itemEntity.price}</dd>
            <dt>
              <span id="count">
                <Translate contentKey="storeApp.item.count">Count</Translate>
              </span>
            </dt>
            <dd>{itemEntity.count}</dd>
            <dt>
              <span id="image">
                <Translate contentKey="storeApp.item.image">Image</Translate>
              </span>
            </dt>
            <dd>
              {itemEntity.image ? (
                <div>
                  <a onClick={openFile(itemEntity.imageContentType, itemEntity.image)}>
                    <img src={`data:${itemEntity.imageContentType};base64,${itemEntity.image}`} style={{ maxHeight: '30px' }} />
                  </a>
                  <span>
                    {itemEntity.imageContentType}, {byteSize(itemEntity.image)}
                  </span>
                </div>
              ) : null}
            </dd>
          </dl>
          <Button tag={Link} to="/entity/item" replace color="info">
            <FontAwesomeIcon icon="arrow-left" />{' '}
            <span className="d-none d-md-inline">
              <Translate contentKey="entity.action.back">Back</Translate>
            </span>
          </Button>
          &nbsp;
          <Button tag={Link} to={`/entity/item/${itemEntity.id}/edit`} replace color="primary">
            <FontAwesomeIcon icon="pencil-alt" />{' '}
            <span className="d-none d-md-inline">
              <Translate contentKey="entity.action.edit">Edit</Translate>
            </span>
          </Button>
        </Col>
      </Row>
    );
  }
}

const mapStateToProps = ({ item }: IRootState) => ({
  itemEntity: item.entity
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ItemDetail);

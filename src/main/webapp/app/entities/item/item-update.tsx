import React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
// tslint:disable-next-line:no-unused-variable
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, setFileData, openFile, byteSize, ICrudPutAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntity, updateEntity, createEntity, setBlob, reset } from './item.reducer';
import { IItem } from 'app/shared/model/item.model';
// tslint:disable-next-line:no-unused-variable
import { convertDateTimeFromServer, convertDateTimeToServer } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IItemUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export interface IItemUpdateState {
  isNew: boolean;
}

export class ItemUpdate extends React.Component<IItemUpdateProps, IItemUpdateState> {
  constructor(props) {
    super(props);
    this.state = {
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
  }

  onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => this.props.setBlob(name, data, contentType), isAnImage);
  };

  clearBlob = name => () => {
    this.props.setBlob(name, undefined, undefined);
  };

  saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const { itemEntity } = this.props;
      const entity = {
        ...itemEntity,
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
    this.props.history.push('/entity/item');
  };

  render() {
    const { itemEntity, loading, updating } = this.props;
    const { isNew } = this.state;

    const { image, imageContentType } = itemEntity;

    return (
      <div>
        <Row className="justify-content-center">
          <Col md="8">
            <h2 id="storeApp.item.home.createOrEditLabel">
              <Translate contentKey="storeApp.item.home.createOrEditLabel">Create or edit a Item</Translate>
            </h2>
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col md="8">
            {loading ? (
              <p>Loading...</p>
            ) : (
              <AvForm model={isNew ? {} : itemEntity} onSubmit={this.saveEntity}>
                {!isNew ? (
                  <AvGroup>
                    <Label for="item-id">
                      <Translate contentKey="global.field.id">ID</Translate>
                    </Label>
                    <AvInput id="item-id" type="text" className="form-control" name="id" required readOnly />
                  </AvGroup>
                ) : null}
                <AvGroup>
                  <Label id="titleLabel" for="item-title">
                    <Translate contentKey="storeApp.item.title">Title</Translate>
                  </Label>
                  <AvField
                    id="item-title"
                    type="text"
                    name="title"
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') },
                      minLength: { value: 3, errorMessage: translate('entity.validation.minlength', { min: 3 }) },
                      maxLength: { value: 20, errorMessage: translate('entity.validation.maxlength', { max: 20 }) }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <Label id="descriptionLabel" for="item-description">
                    <Translate contentKey="storeApp.item.description">Description</Translate>
                  </Label>
                  <AvField
                    id="item-description"
                    type="text"
                    name="description"
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') },
                      minLength: { value: 3, errorMessage: translate('entity.validation.minlength', { min: 3 }) },
                      maxLength: { value: 10240, errorMessage: translate('entity.validation.maxlength', { max: 10240 }) }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <Label id="priceLabel" for="item-price">
                    <Translate contentKey="storeApp.item.price">Price</Translate>
                  </Label>
                  <AvField
                    id="item-price"
                    type="string"
                    className="form-control"
                    name="price"
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') },
                      min: { value: 0, errorMessage: translate('entity.validation.min', { min: 0 }) },
                      number: { value: true, errorMessage: translate('entity.validation.number') }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <Label id="countLabel" for="item-count">
                    <Translate contentKey="storeApp.item.count">Count</Translate>
                  </Label>
                  <AvField
                    id="item-count"
                    type="string"
                    className="form-control"
                    name="count"
                    validate={{
                      required: { value: true, errorMessage: translate('entity.validation.required') },
                      min: { value: 0, errorMessage: translate('entity.validation.min', { min: 0 }) },
                      number: { value: true, errorMessage: translate('entity.validation.number') }
                    }}
                  />
                </AvGroup>
                <AvGroup>
                  <AvGroup>
                    <Label id="imageLabel" for="image">
                      <Translate contentKey="storeApp.item.image">Image</Translate>
                    </Label>
                    <br />
                    {image ? (
                      <div>
                        <a onClick={openFile(imageContentType, image)}>
                          <img src={`data:${imageContentType};base64,${image}`} style={{ maxHeight: '100px' }} />
                        </a>
                        <br />
                        <Row>
                          <Col md="11">
                            <span>
                              {imageContentType}, {byteSize(image)}
                            </span>
                          </Col>
                          <Col md="1">
                            <Button color="danger" onClick={this.clearBlob('image')}>
                              <FontAwesomeIcon icon="times-circle" />
                            </Button>
                          </Col>
                        </Row>
                      </div>
                    ) : null}
                    <input id="file_image" type="file" onChange={this.onBlobChange(true, 'image')} accept="image/*" />
                    <AvInput type="hidden" name="image" value={image} />
                  </AvGroup>
                </AvGroup>
                <Button tag={Link} id="cancel-save" to="/entity/item" replace color="info">
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
  itemEntity: storeState.item.entity,
  loading: storeState.item.loading,
  updating: storeState.item.updating,
  updateSuccess: storeState.item.updateSuccess
});

const mapDispatchToProps = {
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ItemUpdate);

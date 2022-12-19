import classNames from 'classnames';
import React, { useRef, useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';
import { useSelector } from 'react-redux';

// components from container app
import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import IconAvatarNew from 'dna-container/IconAvatarNew';

import { Envs } from '../../../../Utility/envs';
import { withRouter } from 'react-router-dom';

const OtherRelevantInfo = ({ onSave, history, user, isDataProduct }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch,
  } = useFormContext();
  const provideDataProducts = useSelector((state) => state.provideDataProducts);

  const [showInfoModal, setShowInfoModal] = useState(false);
  const [showAddConsumersModal, setShowAddConsumersModal] = useState(false);
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  const addTeamMemberModalRef = useRef();

  const [teamMembers, setTeamMembers] = useState([]);
  const [teamMemberObj, setTeamMemberObj] = useState({
    shortId: '',
    company: '',
    department: '',
    email: '',
    firstName: '',
    lastName: '',
    userType: '',
    teamMemberPosition: '',
  });
  const [editTeamMember, setEditTeamMember] = useState(false);
  const [editTeamMemberIndex, setEditTeamMemberIndex] = useState(-1);

  const isDisabled = !teamMembers.length && !provideDataProducts.selectedDataProduct.users?.length ? true : false;
  const hasUsers = watch('users');

  const [isCreator, setIsCreator] = useState(false);

  const onTeamMemberMoveUp = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index - 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const onTeamMemberMoveDown = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index + 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const onTeamMemberEdit = (index) => {
    setTeamMemberObj(teamMembers[index]);
    setEditTeamMember(true);
    setEditTeamMemberIndex(index);
    setShowAddTeamMemberModal(true);
  };

  const onTeamMemberDelete = (index) => {
    const teamMembersCpy = [...teamMembers];
    teamMembersCpy.splice(index, 1);
    setTeamMembers(teamMembersCpy);
  };

  const updateTeamMemberList = (teamMemberObj) => {
    teamMemberObj['addedByProvider'] = true;
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    setTeamMembers(teamMembers);
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(-1);
  };

  useEffect(() => {
    if (showAddTeamMemberModal) {
      if (!editTeamMember) {
        addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, false);
      } else addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, true);
    }
  }, [showAddTeamMemberModal, teamMemberObj, addTeamMemberModalRef, editTeamMember]);

  useEffect(() => {
    hasUsers?.length && setTeamMembers(hasUsers);
  }, [hasUsers]);

  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(-1);
  };

  const validateMembersList = (teamMemberObj) => {
    let duplicateMember = false;
    duplicateMember = teamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    const isCreator = teamMemberObj.shortId === user.id;
    setIsCreator(isCreator);
    return isCreator || duplicateMember;
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers.length}
        onMoveUp={onTeamMemberMoveUp}
        onMoveDown={onTeamMemberMoveDown}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });
  const resetTeamsState = () => {
    setEditTeamMemberIndex(-1);
    setEditTeamMember(false);
    setTeamMemberObj({
      shortId: '',
      department: '',
      email: '',
      firstName: '',
      lastName: '',
      userType: '',
      mobileNumber: '',
      teamMemberPosition: '',
    });
  };

  const showAddTeamMemberModalView = () => {
    resetTeamsState();
    setShowAddTeamMemberModal(true);
  };

  const addMembersContent = (
    <div className={Styles.addMembersContainer}>
      <p>
        Added members will be informed about your initiated Data Transfer to give their information and finalize the
        Minimum Information Documentation.
      </p>
      <hr />
      <div className={classNames(Styles.firstPanel)}>
        <div className={Styles.teamListWrapper}>
          <div className={Styles.addTeamMemberWrapper}>
            <IconAvatarNew className={Styles.avatarIcon} />
            <button id="AddTeamMemberBtn" onClick={showAddTeamMemberModalView}>
              <i className="icon mbc-icon plus" />
              <span>Add team member</span>
            </button>
          </div>
          {teamMembersList}
        </div>
      </div>
    </div>
  );

  const handleCancel = () => {
    history.push('/datasharing');
    setShowAddConsumersModal(false);
  };

  const handleForwardMinInfo = () => {
    // trigger notification
    setValue('notifyUsers', true);
    !watch('providerFormSubmitted') && setValue('providerFormSubmitted', true);
    setValue('users', teamMembers);
    onSave(watch(), () => {
      setShowAddConsumersModal(false);
      history.push('/');
    });
  };

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Specifying other relevant information</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
          </div>
          <div className={Styles.formWrapper}>
            <div id="otherRelevantInfoDescription" className={classNames('input-field-group area')}>
              <label className="input-label" htmlFor="otherRelevantInfo">
                Please provide any other relevant & app specific restrictions that might apply to the corresponding
                data, examples being individual deletion requirements, antitrust regulations, contractual restrictions
                etc.
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('otherRelevantInfo')}
                rows={50}
                id="otherRelevantInfo"
              />
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.termsOfUseContainer}>
            <div className={classNames(Styles.termsOfUseContent)}>
              <label className={classNames('checkbox', errors?.tou ? 'error' : '')}>
                <span className="wrapper">
                  <input {...register('tou', { required: '*Missing entry' })} type="checkbox" className="ff-only" />
                </span>
                <div
                  className={classNames(Styles.termsOfUseText, 'mbc-scroll')}
                  style={{
                    ...(errors?.tou ? { color: '#e84d47' } : ''),
                  }}
                  dangerouslySetInnerHTML={{
                    __html: Envs.DATA_PRODUCT_TOU_HTML,
                  }}
                ></div>
              </label>
            </div>
            <span className={classNames('error-message', Styles.errorMsg)}>{errors?.tou?.message}</span>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <div className="btn-set">
          <button
            className={'btn btn-primary'}
            type="button"
            onClick={handleSubmit((data) => {
              const isPublished = watch('publish');
              setValue('notifyUsers', isPublished ? true : false);
              onSave(watch());
              reset(data, {
                keepDirty: false,
              });
            })}
          >
            Save
          </button>
          {isDataProduct ? (
            <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={handleSubmit((data) => {
                setValue('notifyUsers', true);
                setValue('publish', true);
                setValue('providerFormSubmitted', true);
                onSave(watch());
                history.push('/dataproductlist');
                reset(data, {
                  keepDirty: false,
                });
              })}
            >
              Publish
            </button>
          ) : (
            <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={handleSubmit((data) => {
                setValue('providerFormSubmitted', true);
                onSave(watch());
                setShowAddConsumersModal(true);
                reset(data, {
                  keepDirty: false,
                });
              })}
            >
              Save and Forward Minimum Information
            </button>
          )}
        </div>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
      <Modal
        title={'Select members of the data receiving side'}
        showAcceptButton={false}
        showCancelButton={false}
        buttonAlignment="right"
        show={showAddConsumersModal}
        content={addMembersContent}
        scrollableContent={false}
        onCancel={handleCancel}
        footer={
          <div className={Styles.footerContainer}>
            <button className="btn btn-secondary" onClick={handleCancel}>
              Skip
            </button>
            <button
              className={isDisabled ? 'btn' : 'btn btn-tertiary'}
              disabled={isDisabled}
              onClick={handleForwardMinInfo}
            >
              Forward Minimum Information
            </button>
          </div>
        }
      />
      <AddTeamMemberModal
        ref={addTeamMemberModalRef}
        editMode={editTeamMember}
        showAddTeamMemberModal={showAddTeamMemberModal}
        teamMember={teamMemberObj}
        onUpdateTeamMemberList={updateTeamMemberList}
        onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
        validateMemebersList={validateMembersList}
        customUserErrorMsg={isCreator ? 'You are the creator and not allowed to consume data product' : ''}
      />
    </>
  );
};

export default withRouter(OtherRelevantInfo);

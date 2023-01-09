import cn from 'classnames';
import React, { useEffect } from 'react';
import Styles from './Card.scss';
// import { withRouter } from 'react-router-dom';
// import AddKPIIcon from 'dna-container/AddKPIIcon';
// import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
// import IconAddKPI from 'components/icons/IconAddKPI';

import { history } from '../../router/History';
import IconWrapper from 'components/icons/IconWrapper';
import IconNameRenderer from 'components/icons/IconNameRenderer';
const classNames = cn.bind(Styles);

export interface IDNACardProps {
  isDisabled: boolean;
  title: string;
  description: string;
  url: string;
  isTextAlignLeft: boolean;
  isSmallCard?: boolean;
  isMediumCard?: boolean;
  tags?: string[];
  isExternalLink?: boolean;
  svgIcon?: JSX.Element | string;
  className?: string;
  upperTag?: string;
}
const DNACard = (props: IDNACardProps) => {
  useEffect(() => {
    // Tooltip.defaultSetup();
  }, []);

  const maxTagItem = 4;

  return (
    <>
      <div
        className={classNames(
          Styles.cardWrapper,
          props.isMediumCard ? Styles.mediumCard : '',
          props.isSmallCard ? Styles.smallCard : '',
          props.isDisabled ? Styles.disabled : '',
          props.className,
        )}
        onClick={() => {
          props.isExternalLink ? window.open(props.url) : history.push(props.url);
        }}
      >
        <div className={Styles.cardHeaderSection}>
          {props.isDisabled ? (
            <button className={'btn btn-primary ' + Styles.comingSoonBtn}>Coming Soon</button>
          ) : props.isExternalLink ? (
            <i className={classNames('icon mbc-icon new-tab', Styles.OpenNewTabIcon)} />
          ) : (
            ''
          )}
          {props.upperTag && !props.isDisabled ? (
            <p className={Styles.upperTag}>{props.upperTag}</p>
          ) : (
            ''
          )}
        </div>
        <div className={Styles.cardIconSection}>
          {props.svgIcon ? (
            typeof props.svgIcon === 'string' ? (
              <IconNameRenderer name={props.svgIcon} />
            ) : (
              props.svgIcon
            )
          ) : (
            <IconWrapper size="100"/>
          )}
        </div>
        <div
          className={
            props.isTextAlignLeft
              ? Styles.cardDescriptonSection
              : Styles.cardDescriptonSection + ' ' + Styles.textCenter
          }
        >
          <div className={Styles.titleSection}>{props.title}</div>
          <div className={Styles.descriptionSection}>{props.description}</div>
        </div>
        <div className={Styles.tagSection}>
          {props.tags?.slice(0, maxTagItem)?.map((item: any) => {
            return (
              <span className={Styles.tagItem} key={item}>
                {item}
              </span>
            );
          })}
          {props?.tags?.length > maxTagItem ? <span className={Styles.tagItem}>...</span> : null}
        </div>
      </div>
    </>
  );
};
export default DNACard;

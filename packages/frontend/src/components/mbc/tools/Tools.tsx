import React, { useState, useEffect } from 'react';
import Styles from './Tools.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Tools-Landing.png';

import { ToolsLandingPageElements } from 'globals/landingPageElements';

const Tools = (props: any) => {
  const tag = props.match.params.tag;
  const [cards, setcards] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);

  const orderedToolsLandingPageElements = ToolsLandingPageElements.sort((a: any, b: any) =>
    a.name.toLowerCase() > b.name.toLowerCase() ? 1 : b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 0,
  ).sort((a: any, b: any) => b.isDnAInternalTool - a.isDnAInternalTool);

  const allTags = Array.from(new Set(Array.prototype.concat.apply([], orderedToolsLandingPageElements.map((item: any) => item.tags)))) as string[];

  const onTagsFilterSelected = (selectedTags: string[]) => {
    setSelectedTags(selectedTags);
    if(selectedTags.length) {
      setcards(orderedToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag))));
    } else {
      setcards(orderedToolsLandingPageElements);
    }
  };

  useEffect(() => {
    let toolsToShowOnLoad: any[] = [];
    let selectedTags: string[] = [];
    switch (tag) {
      case 'lowcode':
        selectedTags = ['No / Low Code'];
        toolsToShowOnLoad = orderedToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag)));
        break;
      case 'prodev':
        selectedTags = ['Coding'];
        toolsToShowOnLoad = orderedToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag)));
        break;
      default:
        toolsToShowOnLoad = orderedToolsLandingPageElements
        break;    
    }
    setSelectedTags(selectedTags);
    setcards(toolsToShowOnLoad);
  }, [tag]);

  return (
    <LandingSummary
      title={'Tools'}
      subTitle={
        'Our standard Data & Analytics for both FC Users and Pro Developers.'
      }
      selectedTags={selectedTags}
      tags={allTags}
      headerImage={headerImageURL}
      isBackButton={true}
      isTagsFilter={true}
      onTagsFilterSelected={onTagsFilterSelected}
    >
      <div className={Styles.toolsPageWrapper}>
        <div className={Styles.toolsWrapper}>
          {cards.filter((card) => card.isAdditinalCard === false).map((card, index) => {
            return (
              <DNACard
                key={index}
                title={card.name}
                id={card.id}
                description={card.description}
                url={card.url}
                isExternalLink={card.isExternalLink}
                isTextAlignLeft={card.isTextAlignLeft}
                isDisabled={card.isDisabled}
                isDetailedPage={card.isDetailedPage}
                isSmallCard={card.isSmallCard}
                isMediumCard={card.isMediumCard}
                svgIcon={card.svgIcon}
                animation={card.animation}
                className="tools"
              />
            );
          })}
        </div>
        <div className={Styles.cardsSeparator}>
          <h5 className="sub-title-text">More Services</h5>
          <hr />
        </div>
        <div className={Styles.toolsWrapper}>
          {cards.filter((card) => card.isAdditinalCard === true).map((card, index) => {
            return (
              <DNACard
                key={index}
                title={card.name}
                id={card.id}
                description={card.description}
                url={card.url}
                isExternalLink={card.isExternalLink}
                isTextAlignLeft={card.isTextAlignLeft}
                isDisabled={card.isDisabled}
                isDetailedPage={card.isDetailedPage}
                isSmallCard={card.isSmallCard}
                isMediumCard={card.isMediumCard}
                svgIcon={card.svgIcon}
                animation={card.animation}
                className="tools"
              />
            );
          })}
        </div>

      </div>
    </LandingSummary>
  );
};

export default Tools;

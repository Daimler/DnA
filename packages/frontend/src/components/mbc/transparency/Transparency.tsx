import React from 'react';
import Styles from './Transparency.scss';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';

const Transparency = () => {
  return (
    <MainPanel title={'Transparency'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.transparencyWrapper}>
        <DNACard
          title={'Portfolio'}
          description={
            'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'
          }
          url={'/portfolio'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
        <DNACard
          title={'Solutions'}
          description={
            'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'
          }
          url={'/allsolutions'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
        <DNACard
          title={'Reports'}
          description={
            'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'
          }
          url={'/allreports'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
        <DNACard
          title={'Data Sharing'}
          description={
            'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'
          }
          url={'/data/datasharing'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
      </div>
    </MainPanel>
  );
};

export default Transparency;

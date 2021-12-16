import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconGearProps {
  className?: string;
}

export const IconGear = (props: IIconGearProps): JSX.Element => {
  return (
    <SvgIcon {...props} viewbox={'0 0 32 32'}>
      <g id="Data@MBC-Solutions" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Data_MBC_App_Home" transform="translate(-70.000000, -907.000000)" fill="#252A33">
          <g id="Modules" transform="translate(12.000000, 148.000000)">
            <g id="Phase" transform="translate(0.000000, 516.000000)">
              <g id="Icons" transform="translate(0.000000, 69.000000)">
                <path
                  d="M73.9994322,184.119666 C70.7880091,184.119666 68.1750372,186.757411 68.1750372,189.999282 C68.1750372,193.241153 70.7880091,195.878898 73.9994322,195.878898 C77.2097197,195.878898 79.8226916,193.241153 79.8226916,189.999282 C79.8226916,186.757411 77.2097197,184.119666 73.9994322,184.119666 L73.9994322,184.119666 Z M73.9994322,197.200637 C70.064644,197.200637 66.8645767,193.971376 66.8645767,189.999282 C66.8645767,186.028334 70.064644,182.797927 73.9994322,182.797927 C77.9330848,182.797927 81.1320165,186.028334 81.1320165,189.999282 C81.1320165,193.971376 77.9330848,197.200637 73.9994322,197.200637 L73.9994322,197.200637 Z M72.5095499,204.596871 C73.5236238,204.701188 74.4718339,204.701188 75.4881789,204.596871 L75.7811588,201.585416 C75.8095483,201.296536 76.0196308,201.060389 76.3012549,201.001925 C77.3982262,200.773802 78.4577232,200.378312 79.4468143,199.824626 C79.7000489,199.683625 80.0134692,199.72604 80.2212806,199.931237 L82.3573084,202.03937 C83.1771979,201.466196 83.9391728,200.810485 84.6284704,200.081408 L82.9216922,197.562883 C82.7604397,197.325589 82.7706599,197.010343 82.9432682,196.783366 C83.6143966,195.906411 84.1515264,194.936601 84.545573,193.899156 C84.6489109,193.627472 84.8976031,193.454373 85.2064812,193.476154 L88.1919236,193.705423 C88.4519717,192.732174 88.610953,191.727974 88.6700032,190.707725 L85.7458822,189.880062 C85.4744783,189.803257 85.2825651,189.561377 85.2678025,189.275937 C85.2155658,188.197224 85.0168391,187.141438 84.6761648,186.13953 C84.5830472,185.862114 84.6795716,185.558332 84.9135013,185.387526 L87.3504581,183.623297 C86.9268864,182.700488 86.4056547,181.818947 85.7969833,180.991284 L83.0329791,182.254559 C82.7763378,182.371486 82.4742733,182.31073 82.2823601,182.102094 C81.5862489,181.342066 80.7890711,180.680624 79.9158093,180.136109 C79.6716593,179.984791 79.5558301,179.690179 79.6251005,179.409324 L80.3723128,176.464358 C79.450221,176.016136 78.4872484,175.667646 77.4992929,175.423474 L76.1888324,178.177001 C76.0661896,178.434929 75.791379,178.583954 75.5120261,178.547271 C74.3503267,178.389075 73.6508088,178.389075 72.4834316,178.547271 C72.2018075,178.582808 71.9304036,178.434929 71.8066253,178.177001 L70.4961648,175.423474 C69.5104805,175.667646 68.5486434,176.016136 67.6276872,176.464358 L68.3737639,179.409324 C68.4430343,179.690179 68.3272051,179.984791 68.0830552,180.136109 C67.2086578,180.680624 66.4103443,181.343212 65.7142332,182.102094 C65.52232,182.309583 65.221391,182.372632 64.9636141,182.254559 L62.2007455,180.991284 C61.5920741,181.815508 61.0731136,182.697049 60.6484063,183.624444 L63.0853632,185.387526 C63.3204284,185.558332 63.4158172,185.86326 63.3226996,186.140677 C62.9831609,187.136852 62.7832986,188.191492 62.7299263,189.275937 C62.7162993,189.561377 62.5243861,189.803257 62.2529823,189.880062 L59.3299968,190.707725 C59.389047,191.72912 59.5468927,192.734467 59.8069408,193.705423 L62.7923833,193.476154 C63.0796852,193.454373 63.3499535,193.627472 63.4555626,193.899156 C63.8450668,194.933162 64.3821967,195.902972 65.0544606,196.784513 C65.2270689,197.01149 65.2361536,197.325589 65.0760367,197.562883 L63.3692584,200.081408 C64.0596917,200.810485 64.8193954,201.466196 65.6404204,202.03937 L67.7764483,199.931237 C67.9819885,199.72604 68.29768,199.683625 68.5509145,199.824626 C69.5411412,200.378312 70.6006383,200.773802 71.6964739,201.001925 C71.9769624,201.060389 72.1870449,201.296536 72.21657,201.585416 L72.5095499,204.596871 Z M73.9994322,206 C73.2965076,206 72.5617867,205.947268 71.8225234,205.846389 C71.5215945,205.805121 71.2888003,205.560949 71.2581397,205.25602 L70.9583463,202.190687 C70.0578305,201.963711 69.1845687,201.638148 68.3499167,201.213999 L66.1775502,203.358815 C65.9595187,203.575475 65.6233867,203.609866 65.3644742,203.442499 C64.13464,202.646934 63.0172283,201.684002 62.045171,200.582362 C61.8441731,200.356531 61.8225971,200.020652 61.9917986,199.769602 L63.7303732,197.201783 C63.2068703,196.460096 62.7662649,195.665678 62.4142348,194.833429 L59.3742844,195.066138 C59.0722199,195.087918 58.7883246,194.896478 58.6974781,194.600721 C58.2693641,193.197592 58.0342988,191.725681 58.0002314,190.226259 C57.9922823,189.923623 58.1898734,189.655378 58.478311,189.575134 L61.4546688,188.731422 C61.5284816,187.859051 61.6885985,187.006169 61.9316128,186.181945 L59.4515039,184.385619 C59.2039473,184.205642 59.1119652,183.87664 59.2300656,183.593492 C59.8001273,182.220168 60.559831,180.931674 61.4876007,179.760107 C61.673836,179.525105 61.9974765,179.446007 62.2711516,179.570959 L65.0828501,180.857161 C65.6642676,180.270231 66.3024641,179.740619 66.9860839,179.275202 L66.2263802,176.277503 C66.1514319,175.980599 66.2877016,175.671085 66.5556987,175.528938 C67.877515,174.82852 69.2810931,174.317249 70.7278233,174.013467 C71.0219388,173.955003 71.3205966,174.101735 71.4511884,174.375713 L72.7854961,177.177386 C73.666707,177.083385 74.3321574,177.084532 75.2110971,177.177386 L76.5431337,174.375713 C76.6737255,174.101735 76.9712477,173.95271 77.2664988,174.013467 C78.7177713,174.317249 80.1213494,174.82852 81.4420301,175.528938 C81.7111628,175.671085 81.8474326,175.980599 81.7724842,176.277503 L81.0127805,179.275202 C81.6952647,179.738326 82.3323256,180.269085 82.9160143,180.857161 L85.7288484,179.570959 C86.0013879,179.447154 86.3250285,179.523959 86.5112638,179.760107 C87.4413046,180.937405 88.2010083,182.227046 88.7676632,183.593492 C88.8857636,183.877787 88.7937816,184.205642 88.5450893,184.385619 L86.066116,186.181945 C86.3102659,187.008462 86.4703828,187.861344 86.5441956,188.731422 L89.5205534,189.575134 C89.8101266,189.656524 90.0077177,189.92477 89.9997686,190.227405 C89.9645656,191.723389 89.7295003,193.195299 89.3013863,194.600721 C89.2105398,194.896478 88.9391359,195.086772 88.62458,195.066138 L85.5846296,194.833429 C85.2303284,195.66797 84.7885874,196.463535 84.2662201,197.20293 L86.003659,199.769602 C86.1751318,200.020652 86.1546913,200.356531 85.9525579,200.582362 C84.9805006,201.684002 83.8630888,202.646934 82.632119,203.442499 C82.3754777,203.608719 82.0382102,203.575475 81.8213142,203.358815 L79.6466766,201.213999 C78.8131601,201.637002 77.9410339,201.963711 77.0405182,202.190687 L76.7395892,205.25602 C76.7100641,205.560949 76.4761344,205.806267 76.1752054,205.846389 C75.433671,205.947268 74.7000857,206 73.9994322,206 L73.9994322,206 Z"
                  id="Fill-1"
                />
              </g>
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};

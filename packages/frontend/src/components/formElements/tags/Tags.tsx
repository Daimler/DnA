import cn from 'classnames';
import * as React from 'react';
import { ITag } from '../../../globals/types';
import Styles from './Tags.scss';

const classNames = cn.bind(Styles);

export interface ITagsFieldProps {
  title: string;
  max: number;
  tags: ITag[];
  setTags: (arr: string[]) => void;
  chips: string[];
  isMandatory?: boolean;
  showMissingEntryError: boolean;
  fixedChips?: string[];
  enableUppercase?: boolean;
  suggestionRender?: (tag: any) => React.ReactNode;
  disableOnBlurAdd?: boolean;
  suggestionPopupHeight?: number;
  isDisabled?: boolean;
  removeTag?: (index: number) => void;
}

export interface ITagsFiledState {
  KEY?: any;
  filteredTags: ITag[];
  chips: string[];
  max: number;
  userInput?: string;
  activeSuggestionIndex: number;
  isFocused: boolean;
}

export default class Tags extends React.Component<ITagsFieldProps, ITagsFiledState> {
  public static getDerivedStateFromProps(props: ITagsFieldProps, state: ITagsFiledState) {
    if (props && props.chips) {
      return {
        chips: props.chips,
      };
    }
    return null;
  }
  constructor(props: ITagsFieldProps) {
    super(props);
    this.state = {
      chips: [],
      KEY: {
        backspace: 8,
        tab: 9,
        enter: 13,
        upArrow: 38,
        downArrow: 40,
      },
      max: this.props.max,
      filteredTags: [],
      userInput: '',
      activeSuggestionIndex: -1,
      isFocused: false,
    };
  }

  public componentDidMount() {
    this.setChips(this.props.chips);
  }
  public render() {
    const chips = this.state.chips?.map((chip: any, index: any) => {
      const canDelete = !this.props.fixedChips?.includes(chip);
      return (
        <div className="chips" key={index}>
          <label className="name">{chip}</label>
          {canDelete ? (
            <span
              className={`close-btn ${this.props.isDisabled ? 'disable' : ''}`}
              onClick={this.deleteChip.bind(null, chip)}
            >
              <i className="icon close" />
            </span>
          ) : null}
        </div>
      );
    });

    const suggestions = this.state.filteredTags?.map((filteredTag, index) => {
      let className = classNames(Styles.suggestion);
      if (index === this.state.activeSuggestionIndex) {
        className += ' ' + classNames(Styles.active);
      }
      return (
        <div
          id={filteredTag.id}
          key={filteredTag.id}
          onMouseDown={this.onSuggestionMouseDown}
          className={className}
          data-value={filteredTag.name}
        >
          {this.props.suggestionRender ? this.props.suggestionRender(filteredTag) : filteredTag.name}
        </div>
      );
    });

    const missingEntryMessage = '*Missing entry';
    const isMaxReached = this.props.max === this.state.chips.length;

    return (
      <div
        id={'tagcontainer_' + this.props.title.replace(' ', '_')}
        className={classNames(
          'input-field-group' + (this.props.showMissingEntryError ? ' include-error' : ''),
          !this.props.isDisabled && this.state.isFocused ? 'focused' : '',
          this.props.showMissingEntryError ? Styles.validationError + ' error' : '',
          this.state.filteredTags?.length ? 'open-suggestion' : '',
        )}
      >
        <label htmlFor="tag" className="input-label">
          {this.props.title}
          {this.props.isMandatory ? <sup>*</sup> : ''}
        </label>
        <div
          className={classNames(
            'input-field ' + Styles.tagParent + ' ' + (this.state.chips.length !== 0 ? Styles.haveChips : ''),
            this.props.isDisabled
              ? this.state.chips?.length
                ? Styles.tagParentDisabled
                : Styles.tagParentDisabledFocus
              : '',
          )}
          onClick={this.focusInput}
        >
          <div className={classNames(Styles.row)}>{chips}</div>
          <input
            className={classNames(Styles.tagInputField)}
            type="text"
            id="tag"
            placeholder={!isMaxReached && !this.props.isDisabled ? 'Type here' : ''}
            onKeyDown={this.onKeyDown}
            onChange={this.onTextInputChange}
            autoComplete="off"
            value={this.state.userInput}
            onFocus={this.onTagFieldFocus}
            onBlur={this.onTagFieldBlur}
            readOnly={isMaxReached || this.props.isDisabled}
          />
        </div>
        {suggestions?.length ? (
          this.props.suggestionRender ? (
            <div
              className="mbc-scroll"
              style={{
                overflowY: 'auto',
                ...(this.props.suggestionPopupHeight && { height: this.props.suggestionPopupHeight }),
              }}
            >
              {suggestions}
            </div>
          ) : (
            suggestions
          )
        ) : (
          <span className={classNames('error-message', this.props.showMissingEntryError ? '' : 'hide')}>
            {missingEntryMessage}
          </span>
        )}
      </div>
    );
  }

  protected onTagFieldFocus = () => {
    this.setState({ isFocused: true });
  };

  protected onTagFieldBlur = (event: React.FocusEvent<HTMLInputElement>) => {
    const target = event.target as HTMLInputElement;
    if (!this.props.disableOnBlurAdd) {
      if (target.value) {
        this.updateChips(target.value);
      }
    } else {
      this.setState({ userInput: '', filteredTags: [] });
    }

    this.setState({ isFocused: false });
  };

  protected onSuggestionMouseDown = (event: React.MouseEvent) => {
    const target = event.currentTarget as HTMLElement;
    const userInput = target.getAttribute('data-value');
    if (target.id && target.id !== '0') {
      this.setState({
        userInput,
      });
      this.updateChips(userInput);
    }
  };

  protected onTextInputChange = (event: React.ChangeEvent) => {
    const target = event.target as HTMLInputElement;
    const userInput = target.value;
    const tags = this.props.tags;
    if (userInput) {
      let filteredTags = tags?.filter((tag) => tag.name.toLowerCase().indexOf(userInput.toLowerCase()) > -1);
      if (filteredTags?.length === 0 && tags?.length) {
        filteredTags = [{ id: '0', name: 'No suggestions available' }];
      }
      this.setState({
        filteredTags,
        userInput,
      });
    } else {
      this.setState({
        filteredTags: [],
        userInput,
        activeSuggestionIndex: -1,
      });
    }
  };

  protected focusInput = (event: React.MouseEvent) => {
    const target = event.target as HTMLElement;
    const children = target.children;

    if (children?.length) {
      // @ts-ignore
      children[children.length - 1].focus();
    }
  };
  protected setChips = (chips: string[]) => {
    if (chips && chips?.length) {
      this.setState({ chips });
    }
  };

  protected onKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const target = event.target as HTMLInputElement;
    this.setState({
      userInput: target.value,
    });
    // @ts-ignore
    if (keyPressed === this.state.KEY.enter || (keyPressed === this.state.KEY.tab && target.value)) {
      event.preventDefault();
      this.updateChips(target.value);
    } else if (keyPressed === this.state.KEY.backspace) {
      const chips = this.state.chips;

      if (!target.value && chips?.length) {
        this.deleteChip(chips[chips.length - 1]);
      }
    } else if (keyPressed === this.state.KEY.upArrow) {
      const activeSuggestion =
        this.state.activeSuggestionIndex < 0 ? this.state.activeSuggestionIndex : this.state.activeSuggestionIndex - 1;
      const filteredTags = this.state.filteredTags;
      const userInput = filteredTags[activeSuggestion] ? filteredTags[activeSuggestion].name : this.state.userInput;
      if (this.state.activeSuggestionIndex === 0) {
        this.setState({
          userInput,
        });
        return;
      }
      this.setState({ activeSuggestionIndex: activeSuggestion, userInput });
    } else if (keyPressed === this.state.KEY.downArrow) {
      const activeSuggestion = this.state.activeSuggestionIndex + 1;
      const filteredTags = this.state.filteredTags;
      if (this.state.activeSuggestionIndex === filteredTags?.length) {
        return;
      }
      const userInput = filteredTags[activeSuggestion] ? filteredTags[activeSuggestion].name : this.state.userInput;
      this.setState({ activeSuggestionIndex: activeSuggestion, userInput });
    }
  };

  protected updateChips = (value: string) => {
    if (!this.props.max || this.state.chips?.length < this.props.max) {
      // const value = this.state.userInput;

      if (!value) {
        return;
      }

      if (this.props.enableUppercase) {
        value = value.toUpperCase();
      }

      const chip = value.trim();

      if (chip && this.state.chips.indexOf(chip) < 0) {
        const chips = this.state.chips;
        chips.push(chip);
        this.props.setTags(chips);
        this.setState({
          chips,
        });
      }
    }
    // target.value = '';

    this.setState({
      userInput: '',
      filteredTags: [],
      activeSuggestionIndex: -1,
    });
  };

  protected deleteChip = (chip: string) => {
    const isFixedChip = this.props.fixedChips?.includes(chip);
    if (isFixedChip) {
      return;
    }
    const index = this.state.chips.indexOf(chip);
    const chips = this.state.chips;
    if (index >= 0) {
      chips.splice(index, 1);
      if (this.props.removeTag !== undefined) {
        this.props.removeTag(index);
      } else {
        this.props.setTags(chips);
      }
      this.setState({
        chips,
        filteredTags: [],
        activeSuggestionIndex: 0,
      });
    }
  };
}

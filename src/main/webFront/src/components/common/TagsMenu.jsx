import "./TagsMenu.css"
import {useState} from "react";

const TagsMenu = ({tagList, totalCount, selected, onClickTag}) => {
  return (
      <div className={"tags-menu"}>
        <div className={"tags-menu-header"}>
          태그 목록
        </div>

        <div className={"tags-menu-body"}>
          <div className={"tags-body-item"}>
            <span className={`tags-body-item-text ${selected === "전체보기" ? "tags-body-item-text-selected" : ""}`}
                  onClick={()=>onClickTag("전체보기")}
            >전체보기</span>
            <span>({totalCount})</span>
          </div>

          {tagList && tagList.map(tag=> (
            <div key={tag.tagId} className={"tags-body-item"}>
              <span className={`tags-body-item-text ${selected === tag.tagId ? "tags-body-item-text-selected" : ""}`}
                    onClick={()=>onClickTag(tag.tagId)}
              >{tag.tagName}</span>
              <span>({tag.count})</span>
            </div>
          ))}

          {/*<div className={"tags-body-item"}>*/}
          {/*  <span className={"tags-body-item-text"}>태그1</span>*/}
          {/*  <span>(0)</span>*/}
          {/*</div>*/}

        </div>

      </div>
  )
}

export default TagsMenu;
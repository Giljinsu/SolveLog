import "./TagsMenuMin.css"


// 화면 크기 줄어들었을때 사용
const TagsMenuMin = ({tagList, totalCount, selected, onClickTag}) => {


  return(
      <div className={"tags-menu-min"}>
        <div className={`tags-menu-min-item ${selected === "전체보기" ? "tags-menu-min-item-selected" : ""}`}
             onClick={()=>onClickTag("전체보기")}
        >
          전체보기 ({totalCount})
        </div>
        {tagList && tagList.map(tag => (
          <div key={tag.tagId} className={`tags-menu-min-item ${selected === tag.tagId ? "tags-menu-min-item-selected" : ""}`}
               onClick={()=>onClickTag(tag.tagId)}
          >
            {tag.tagName} ({tag.count})
          </div>
        ))}

      </div>
  )
}

export default TagsMenuMin;
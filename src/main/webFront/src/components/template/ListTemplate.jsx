import './ListTemplate.css'
import testImage from '../../assets/Logo-Test.png'
import {useEffect} from "react";

const ListTemplate = ({header, subHeader, listTemplate, listPlaceholder, leftMenuMax, leftMenuMin}) => {

  // 임시
  // title = "임시작성글";
  // listPlaceholder = "임시 저장한 글이 없습니다.";


  return (
      <div className={"listTemplate"}>
        <div className={"list-header"}>
          <div className={"list-header-title"}>
            <h1>{header}</h1>
          </div>
        </div>
        <div className={"list-subheader"}>
          {subHeader}
        </div>
        <div className={"list-body"}>
          <div className={"list-left-menu-max"}>
            {leftMenuMax}
          </div>
          <div className={"list-left-menu-min"}>
            {leftMenuMin}
          </div>
          {listTemplate && listTemplate.length > 0 ? listTemplate :
              (<div className={"no-list-template"}>{listPlaceholder}</div>)
          }
        </div>
        <div className={"list-footer"}>

        </div>

      </div>
  )
}

export default ListTemplate;
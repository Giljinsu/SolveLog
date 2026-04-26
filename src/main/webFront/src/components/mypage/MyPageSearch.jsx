import "./MyPageSearch.css"
import {useState} from "react";

// isMyPage : 로그인한 유저와 해당 마이페이지가 일치하면 true
const MyPageSearch = ({onClickButton, isMyPage, tabSelected , setTabSelected
  ,subTabList, subTabSelected , setSubTabSelected, searchValue, setSearchValue}) => {
  const subTabIndex = subTabList.indexOf(subTabSelected);
//   const [searchValue, setSearchValue] = useState("");
  // const [selected, setSelected] = useState(1)

  return (
        <div className={"my-page-search-section"}>
          {isMyPage && (
            <div className={"my-page-search-tab-section"}>
              <div className={"my-page-search-tab"}>
                <span
                    className={"my-page-search-tab-item "
                        + `${tabSelected === "my" ? "my-page-search-tab-item-selected" : ""}`}
                    onClick={()=>setTabSelected("my")}
                >
                  내 글
                </span>
                <span
                    className={"my-page-search-tab-item "
                        + `${tabSelected === "like" ? "my-page-search-tab-item-selected" : ""}`}
                    onClick={()=>setTabSelected("like")}
                >
                  좋아요한 글
                </span>
                <span
                    className={"my-page-search-tab-item "
                        + `${tabSelected === "statistics" ? "my-page-search-tab-item-selected" : ""}`}
                    onClick={()=>setTabSelected("statistics")}
                >
                  통계
                </span>
                <div
                    className={"underline"}
                    style={{
//                           transform: tabSelected === "my" ? "translateX(0%)" : "translateX(100%)"
                          transform: tabSelected === "my" ? "translateX(0%)" : tabSelected === "like" ? "translateX(100%)" : "translateX(200%)"
                        }}
                >
                </div>
              </div>
            </div>
          )}

           <div className={"my-page-search-sub-tab-section"}>
             <div className={"my-page-search-sub-tab"}>
               {subTabList?.map(subTab => (
                 <span
                      key={subTab}
                      className={"my-page-search-sub-tab-item "
                          + `${subTabSelected === subTab ? "my-page-search-sub-tab-item-selected" : ""}`}
                      onClick={()=>setSubTabSelected(subTab)}
                  >
                    {subTab}
                  </span>
               ))}
               <div
                   className={"underline"}
                   style={{
                         width: `${100 / subTabList.length}%`,
                         transform: `translateX(${subTabIndex * 100}%)`,
                       }}
               >
               </div>
             </div>
           </div>

         {tabSelected !== "statistics" ? (
          <span className={"my-page-search"}>
            <input
                placeholder={"검색어를 입력하세요"}
                value={searchValue || ""}
                onChange={(e)=>setSearchValue(e.target.value)}
            />
            <button onClick={()=>onClickButton(searchValue)}>
              🔍
            </button>
          </span>
         ): ""}
        </div>
    )
}

export default MyPageSearch;
import "./SideMenu.css"
import {AiFillHeart, AiOutlineHeart} from "react-icons/ai"
import {useEffect} from "react";

export const LeftSideMenu = ({leftMenuRef, likes, onLikeButtonClicked, likeBoolean}) => {

  // 위로 이동
  const onUpButtonClicked = () => {
    window.scrollTo( {top:0, behavior:"smooth"});
  }


  // 아래로 이동
  const onDownButtonClicked = () => {
    const commentSection = document.getElementById("post_comment_section");
    if (commentSection) {
      commentSection.scrollIntoView({behavior:"smooth"})
    }
  }


  return(
      <>
        {/*<div className={"side_menu_parent"}>*/}
        <div ref={leftMenuRef} className={"side_menu_backdrop"}>
          <section className={"left_menu_section"}>
            <div className="toolbar">
              <button className="remote-button"
                      onClick={onUpButtonClicked}>↑</button>
              <div className={"likeButton"}>
                <button className="like-remote-button"
                        onClick={() => onLikeButtonClicked()}>
                  {
                    likeBoolean ?
                      <AiFillHeart size={24} color="#e11d48"/> :
                      <AiOutlineHeart size={24}/>
                  }
                </button>
                <div className={"likeText"}>{likes}</div>
              </div>
              <button className="remote-button"
                      onClick={onDownButtonClicked}>↓</button>
            </div>
          </section>
        </div>
          {/*</div>*/}
      </>
  )
}

export const RightSideMenu = ({rightMenuRef, titleList, onClickedRightMenu, rightMenuSelectedIndex}) => {


  if (!titleList) {
    return (<div>로딩중</div>)
  }

  return (
      <>
        <div ref={rightMenuRef} className={"side_menu_backdrop"}>
          <section className={"right_menu_section"}>
            <div className={"title_list"}>
              {titleList && titleList.map((titles,index) => (
                  <div
                      key={index}
                      className={`title_list_${titles.level} ${rightMenuSelectedIndex === index ? 'right-menu-selected' : ''}`}
                      onClick={() => onClickedRightMenu(titles.id)}
                  >
                    {titles.text}
                  </div>
              ))}
            </div>
          </section>
        </div>
      </>
  )
}
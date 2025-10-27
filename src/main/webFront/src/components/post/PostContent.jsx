import "./PostContent.css"
import {
  Children,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState
} from "react";
import PostComment from "./PostComment.jsx";
import {Button2} from "../common/Button.jsx";
import {getKorDate} from "../../utils/DateUtils.js";
import {LeftSideMenu, RightSideMenu} from "../common/SideMenu.jsx";
import {useAuth} from "../../context/AuthContext.jsx";
import {AiFillHeart, AiOutlineHeart} from "react-icons/ai";
import axios from "../../context/axiosInstance.js";
import {LoginDispatchContext} from "../../App.jsx";
import MarkdownRenderer from "../common/MarkdownRenderer.jsx";
import {useNavigate} from "react-router-dom";
import {useSearchContext} from "../../context/SearchContext.jsx";
import UserImg from "../common/UserImg.jsx";

const PostContent = ({postId, title, author, authorImg, authorBio, postUsername, writtenDate, content, likes,
  commentCnt, comments, viewCount, tags, files, targetCommentIdForScroll,
  onCommentChange, onClickCommentCreate, onClickCommentDelete,
  getLikesCount, deletePost}) => {
  const leftMenuRef = useRef(null);
  const rightMenuRef = useRef(null);
  const contentRef = useRef(null);
  const menuRef = useRef(null);
  // const [thumbnail, setThumbnail] = useState('');
  const [titleList, setTitleList] = useState([]);
  const [likeBoolean, setLikeBoolean] = useState(false)
  const [likeVisible, setLikeVisible] = useState(false);
  const [postMenuIsOpen, setPostMenuIsOpen] = useState(false);
  const [curComment, setCurComment] = useState({
    postId : postId,
    // userId : "",
    username : "",
    comment : ""
  });
  const nav = useNavigate();
  const {setIsLoginOpen} = useContext(LoginDispatchContext)
  const {user, isAuthentication, isLoading} = useAuth();
  const {resetSearchConditionForTag} = useSearchContext();

  const titleListRef = useRef([]); // 2025 07 18 추가
  const [rightMenuSelectedIndex, setRightMenuSelectedIndex] = useState(-1);
  const rightMenuSelectedIndexRef = useRef(-1);

  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

  useEffect(() => {
    if (isLoading) return ; // 인증

    if (user) {
      setCurComment({...curComment, username : user.username})
    }

  }, [isLoading]);

  useEffect(() => {
    titleListRef.current = titleList;
  }, [titleList]);

  // useEffect(() => {
  //   console.log("selected index changed to:", rightMenuSelectedIndex);
  // }, [rightMenuSelectedIndex]);




  // 좋아요 버튼 클릭시
  const onLikeButtonClicked = async () => {
    if (!isAuthentication) {
      setIsLoginOpen(true);
      return;
    }

    try {
      if (!likeBoolean) {
        await axios.post("/api/createLike",{
          username: user.username,
          postId: postId
        })
      } else {
        await axios.post("/api/deleteLike", {
          username: user.username,
          postId: postId
        })
      }

    } catch (e) {
      setLikeBoolean(false);
    } finally {
      await getIsLiked();
      await getLikesCount();
    }

  }

  // 해당 유저 좋아요 여부
  const getIsLiked = async () => {
    try {
      const axiosResponse = await axios("/api/isLiked",
          {
            params: {
              username: user.username,
              postId : postId
            }
          }
      );

      setLikeBoolean(axiosResponse.data);
    } catch (e) {
      console.log(e);
      setLikeBoolean(false);
    }

  }

  // content 내 h1 or # 의 제목 요소 리스트를 가져옴
  const getTitleList = () => {
    if (!contentRef.current) return;
    const headings = contentRef.current.querySelectorAll("h1, h2, h3"); // 순서 보장 o

    const titleList = Array.from(headings).map((el) => ({
      level: el.tagName,
      text: el.textContent,
      id: el.id || null,
    }));

    // titleListRef.current = titleList;
    setTitleList(titleList);

  }

  // 클릭시 해당 위치로 이동
  const onClickedRightMenu = (id) => {
    const element = document.getElementById(id);

    if (element) {
      element.scrollIntoView({behavior:"smooth"});
    }
  }

  // 마크다운 제목태그 아이디 생성
  const generateId = (children) => {
    // 들어오는 값이 문자열이 아닌경우가 있을수 있으니
    // const text =
    //     typeof children === "string"
    //         ? children
    //         : Children.toArray(children).join(" ");

    // 들어오는 값이 문자열이 아닌경우 React Element 가 들어오는경우
    // 재귀적으로 String type 만 리턴
    const text = Children.toArray(children)
      .map(child => {
        if (typeof child ==="string" || typeof child === "number") {
          return child;
        }

        if (child.props && child.props.children) {
          return generateId(child.props.children)
        }
        return "";
      }).join();

    return text
      // .toString()
      .toLowerCase()
      .replace(/[^a-z0-9가-힣]+/g,'-')
      .replace(/^-+|-+$/g, '');
  }

  const headingRenderer = useCallback((Header) => ({node, ...props}) => {
    const newId = generateId(props.children);

    return (<Header id={newId} {...props}/>)
  },[]);

  // 메뉴 및 배경색 관리
  useEffect(() => {
    document.body.style.backgroundColor = 'white';
    const leftMenu = leftMenuRef.current;
    const rightMenu = rightMenuRef.current;
    const content = contentRef.current;

    const absoluteTop = leftMenu.getBoundingClientRect().top + window.scrollY;
    //엘리먼트의 상단이 브라우저 화면(Viewport)의 최상단으로부터 얼마나 떨어져 있는지를 픽셀(px)로 반환합니다.

    const onsScrollEvent = () => {
      if (!leftMenu || !content) return;

      const contentTop = content.offsetTop;
      const scrollY = window.scrollY;

      // contentTop 사용불가 css 가 변경되면 top 이 변동이 됨
      if (absoluteTop + scrollY > contentTop) {
        leftMenu.className = "side_menu_backdrop"
        if(rightMenu) rightMenu.className = "side_menu_backdrop"
      } else if (absoluteTop + scrollY <= contentTop) {
        leftMenu.className = "side_menu_backdrop_inactive"
        if(rightMenu) rightMenu.className = "side_menu_backdrop_inactive"
      }

    }

    window.addEventListener('scroll', onsScrollEvent);
    onsScrollEvent();

    // 추가함 2025-07-18
    // right sidemenu의 제목 하이라이트
    let index = 0;
    let prevOffsetTop = 0;
    let order =-1;
    const onScrollRightMenuHighlightEvent = () => {
      if (titleListRef.current.length < 1) return;
      // if (titleListRef.current.length-1 < index) return;

      const scrollY = window.scrollY;
      const curTitle = titleListRef.current[index];
      const curElement = document.getElementById(curTitle.id);
      if (!curElement) return; // DOM이 없으면 그냥 함수 종료
      const curOffsetTop = curElement.offsetTop;

      // console.log(scrollY);
      // console.log(prevOffsetTop);
      // console.log(curOffsetTop);

      if (scrollY > prevOffsetTop && scrollY < curOffsetTop ) {
        order = index-1;
      } else {

        // 스크롤 내리기
        if (scrollY >= curOffsetTop) {
          if (titleListRef.current.length-1 > index) {
            prevOffsetTop = curOffsetTop;
            index++;
          }
          if (titleListRef.current.length-1 > order) {
            order++;
          }

        }

        // 스크롤 올림
        if (scrollY < prevOffsetTop) {
          if (order > -1) order--;
          if (index > 0) {
            index === 1 ?
                prevOffsetTop = 0 :
                prevOffsetTop = document.getElementById(titleListRef.current[index-1].id).offsetTop;
            index--;
          }
        }

      }

      if (rightMenuSelectedIndexRef.current !== order) {
        rightMenuSelectedIndexRef.current = order;
        setRightMenuSelectedIndex(order);
      }

    }

    window.addEventListener('scroll', onScrollRightMenuHighlightEvent);
    onScrollRightMenuHighlightEvent();


    // 화면 크기에 따른 like visible 설정
    const handleResize = () => {
      if (window.innerWidth < 1439) {
        setLikeVisible(true);
      } else {
        setLikeVisible(false)
      }
    }

    window.addEventListener('resize', handleResize);
    handleResize();

    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setPostMenuIsOpen(false);
      }
    };

    window.addEventListener('mousedown', handler);

    return () => {
      window.removeEventListener('scroll', onsScrollEvent);
      window.removeEventListener('scroll', onScrollRightMenuHighlightEvent);
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('mousedown', handler);

      document.body.style.backgroundColor = '#fdfdfd';
    }
  }, []);


  // 유저 인증 로딩 관리
  useEffect(() => {
    if (isLoading) return;
    if (!isAuthentication) {
      setLikeBoolean(false);
      return;
    }


    getIsLiked();
  }, [isLoading]);

  // 내용 변경시
  useEffect(() => {
    if (!content) return;

    getTitleList();
  }, [content]);

  //썸네일
  const thumbnail = useMemo(() => {
    if (!files) return;
    const thumbnailFile = files.filter(file => {

      if (file.isThumbnail){
        return file.fileId;
      }
    });

    const fileId = thumbnailFile[0].fileId;

    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

    return `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`

  }, [files]);

  const markdownContent = useMemo(() => {
    return `${thumbnail || ''}${content}`;
  }, [thumbnail, content]);

  const onClickAuthor = () => {
    nav(`/myPage/${postUsername}`,{
      state:{
        nickname : author
      }
    })
  }


  // if (titleList.length < 1) return (<div>로딩중</div>);

  return (
      <>
        <div className={"post_content"}>

          <section className={"post_content_header"}>
            <div className={"post_content_header_title_section"}>
              <h1>{title}</h1>
              {isAuthentication && user.username === postUsername && (
                  <div ref={menuRef}
                       className={"post_content_header_title_button_section"}>
                    <button onClick={(e) => {
                      setPostMenuIsOpen(!postMenuIsOpen);
                    }}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="20"
                           height="20"
                           fill="currentColor" viewBox="0 0 24 24">
                        <circle cx="12" cy="5" r="2"/>
                        <circle cx="12" cy="12" r="2"/>
                        <circle cx="12" cy="19" r="2"/>
                      </svg>
                    </button>
                    {postMenuIsOpen && (
                        <ul className={"post_content_header_title_dropdown_menu"}>
                          <li onClick={() => {
                            nav("/postEdit", {
                              state: {
                                postId: postId,
                                isTempButtonVisible: false
                              }
                            })
                          }}>게시글 수정
                          </li>
                          <li onClick={() => deletePost(postId)}>게시글 삭제</li>
                        </ul>
                    )}
                  </div>
              )}
            </div>

            <div className={"post_content_header_sub"}>
              <div className={"post_content_header_sub_info"}>
                <span className={"post_author"}
                      onClick={() => onClickAuthor()}>{author}</span>
                <span className={"post_written_date"}>{getKorDate(
                    writtenDate)}</span>
              </div>

              <div className={"post_content_header_sub_cnt"}>
                <span>조회수 : {viewCount}</span>
                <div className={"post_content_like_box"}>
                  {
                    likeVisible ?
                        <span className="post_content_like_button"
                              onClick={() => onLikeButtonClicked()}>
                          {
                            likeBoolean ?
                                <AiFillHeart size={17} color="#e11d48"/> :
                                <AiOutlineHeart size={17}/>
                          }
                        </span>
                        : <span className={"post_content_like_text"}>좋아요</span>
                  }
                  <span> : {likes}</span>
                </div>
              </div>
            </div>
            <div className={"post_tag_section"}>
              {/*{tags && tags.split(",").map(*/}
              {/*    tag => <span className="tag" key={tag}>#{tag}</span>)}*/}
              {tags && tags.map(
                  tag =>
                      <span
                          className="tag"
                          key={tag.tagId}
                          onClick={() => {
                            resetSearchConditionForTag();
                            nav("/", {
                              state: {tagId: tag.tagId}
                            });

                            // setTimeout(() =>{
                            //   getPageByTagId(tag.tagId);
                            // },100)
                          }}
                      >#{tag.tagName}</span>)}
            </div>
          </section>

          <section ref={contentRef} className={"post_content_body"}>
            <div className={"post_left_menu"}>
              <LeftSideMenu
                  leftMenuRef={leftMenuRef}
                  likes={likes}
                  onLikeButtonClicked={onLikeButtonClicked}
                  likeBoolean={likeBoolean}
              />
            </div>

            <div className={'post_right_menu'}
                 style={{display: titleList.length < 1 && 'none'}}
            >
              <RightSideMenu
                  rightMenuRef={rightMenuRef}
                  titleList={titleList}
                  onClickedRightMenu={onClickedRightMenu}
                  rightMenuSelectedIndex={rightMenuSelectedIndex}
              />
            </div>

            <MarkdownRenderer content={markdownContent}
                              headingRenderer={headingRenderer}/>


          </section>

          <section className={"post_footer_section"}>
            {/* 작성자 프로필 */}
            <div className={"post-footer"}>
              <UserImg
                  radius={120}
                  nickname={author}
                  userImg={authorImg && authorImg.fileId
                      ? `${backendBaseUrl}/api/inlineFile/${authorImg.fileId}`
                      : ""}
                  onClickImg={onClickAuthor}
              />
              <div className="author-info">
                <div className="name"
                     onClick={()=>onClickAuthor()}
                     // style={{cursor : "pointer"}}
                >{author}</div>
                {/*<div className="role">Solvelog 개발자</div>*/}
                {authorBio && (
                    <div className="bio">{authorBio}</div>
                )}
              </div>
            </div>

          </section>

          <section id={"post_comment_section"}
                   className={"post_comment_section"}>
            <h3>댓글 {commentCnt}개</h3>
            {comments && comments.map(comment =>
                (
                    <div key={comment.commentId}
                         className={"post_comment_comments"}>
                      <PostComment
                          key={comment.commentId}
                          commentId={comment.commentId}
                          commentAuthor={comment.nickname}
                          writtenDate={comment.createdDate}
                          onClickCommentDelete={onClickCommentDelete}
                          comment={comment.comment}
                          childComments={comment.childComments}
                          postId={postId}
                          commentUsername={comment.username}
                          commentUserImg={comment.userImg}
                          onCommentChange={onCommentChange}
                          onClickCommentCreate={onClickCommentCreate}
                          targetCommentIdForScroll={targetCommentIdForScroll}
                      />
                    </div>
                )
            )}

            <div className={"post_comment_input"}>
              <textarea
                  name={"comment"}
                  onChange={onCommentChange(curComment, setCurComment)}
                  value={curComment.comment}
                  placeholder="댓글을 작성해주세요."
                  rows={4}
              />

              <Button2
                  buttonEvent={() => onClickCommentCreate(curComment,
                      setCurComment)}
                  buttonText={"댓글 작성"}
              />
            </div>

          </section>

        </div>
        <div className={"post_end_buffer"} style={{"height":"80px"}} />

      </>
  )
}

export default PostContent
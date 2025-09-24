import "./PostCard.css"
import noImage from '../../assets/no-image.png'
import testImage2 from '../../assets/test2.webp'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/ko';
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";
import {useSearchContext} from "../../context/SearchContext.jsx";
import UserImg from "./UserImg.jsx";


function PostCard({postId, title, tags, category, summary, author, date, views, likes,
  username, userImg, commentCnt, thumbnail, onClickTag}) {
  let nav = useNavigate();
  const {getPageByTagId} = useSearchContext();
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

  // tags = "태그, 태그2";
  // category = "카테고리";

  // dayjs 설정
  dayjs.extend(relativeTime);
  dayjs.locale('ko'); // 한국어 설정

  const now = dayjs(); // 현재 시각
  const postDate = dayjs(date);
  // 끝

  const onPostClick = () => {
    nav(`/post/${title}`, {
      state : {
        postId : postId
      }
    })
  }

  const onClickAuthor = (e) => {
    e.stopPropagation();
    nav(`/myPage/${username}`, {
      state:{
        nickname:author
      }
    })
  }

  return (
      <div
          className="post-card"
          onClick={onPostClick}
      >
        <div className={"card-image"}>
          <img alt={"missing"} src={thumbnail ? thumbnail : noImage}/>
        </div>
          <div className="meta">
            <span>♥ {likes}</span>
            <span>👁 {views}</span>
            <span>💬 {commentCnt}</span>
          </div>
        <div className="card-header">
          <h3>{title}</h3>
        </div>

        <div className="card-body">
          <div className="tags">
            {/*{tags && tags.split(",").map(tag => <span className="tag" key={tag}>#{tag}</span>)}*/}
            {tags && tags.map(tag =>
                <span
                    className="tag"
                    key={tag.tagId}
                    onClick={(e)=>{
                      e.stopPropagation();
                      if (onClickTag) {
                        onClickTag(tag.tagId);
                      } else {
                        getPageByTagId(tag.tagId);
                      }
                    }}
                >#{tag.tagName}</span>
            )}
          </div>
          <div className="category">{category}</div>
          <p className="summary">{summary}</p>
        </div>

        <div className="card-footer">
          {author && (
            <section className={"card-footer-author-section"} onClick={(e)=> onClickAuthor(e)}>
              <UserImg
                  radius={24}
                  nickname={author}
                  // userImg={userImg}
                  userImg={userImg && userImg.fileId ? `${backendBaseUrl}/api/inlineFile/${userImg.fileId}` : ""}
              />
              <span className="author">{author}</span>
            </section>
          )}
          <span className="date">{postDate.from(now)}</span>

        {/*  날짜는 dayjs나 date-fns로 포맷팅해서 "3일 전" 같은 형태 추천*/}
        </div>
      </div>
  );
}

export default PostCard;
import "./PageList.css"
import PostCard from "./common/PostCard.jsx";
import {useEffect} from "react";

const PageList = ({postList}) => {
  const getImage = (imgId) => {
    if (!imgId) return null;

    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

    return`${backendBaseUrl}/api/inlineFile/${imgId}`;
  }


  return (
      <>
        <div className={"post-list"}>
          {postList && postList.map(post => (
                <PostCard
                    key={post.id}
                    postId={post.id}
                    title={post.title}
                    summary={post.summary}
                    author={post.nickName}
                    date={post.createdDate}
                    category={post.categoryType}
                    views={post.viewCount}
                    likes={post.likeCount}
                    commentCnt={post.commentCount}
                    // tags={post.tags}
                    tags={post.tagList}
                    username={post.username}
                    userImg={post.userImg}
                    thumbnail={getImage(post.thumbnailFile.fileId)}
                />
              )
          )}
        </div>
      </>
  )
}

export default PageList;
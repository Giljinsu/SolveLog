import PostCard from "../common/PostCard.jsx";
import "./MyPagePostList.css"

const MyPagePostList = ({post, onClickTag, tabSelected}) => {
  const getImage = (imgId) => {
    if (!imgId) return null;

    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

    return`${backendBaseUrl}/api/inlineFile/${imgId}`;
  }


  return (
      <div className={"my-page-post-cards"}>
        <PostCard
            postId={post.id}
            title={post.title}
            summary={post.summary}
            // author={post.nickName}
            date={post.createdDate}
            category={post.categoryType}
            views={post.viewCount}
            likes={post.likeCount}
            commentCnt={post.commentCount}
            tags={post.tagList}
            thumbnail={getImage(post.thumbnailFile.fileId)}
            author={tabSelected !== "my" ? post.nickName : null}
            username={tabSelected !== "my" ? post.username : null}
            onClickTag={onClickTag}
        />
      </div>
  )
}

export default MyPagePostList;
import "./PostComment.css"
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import {useAuth} from "../../context/AuthContext.jsx";
import {Button2} from "../common/Button.jsx";
import {use, useEffect, useRef, useState} from "react";
import UserImg from "../common/UserImg.jsx";
import userImg from "../common/UserImg.jsx";
import {useNavigate} from "react-router-dom";

const PostComment = ({commentId, commentAuthor, writtenDate, comment, onClickCommentDelete,
  parentCommentId, childComments, postId, onClickCommentCreate, onCommentChange, commentUsername,
  targetCommentIdForScroll, commentUserImg}) => {
  // targetCommentIdForScroll
  dayjs.extend(utc)
  dayjs.extend(timezone)
  dayjs.extend(relativeTime);// .from .to .fromNow .toNow 를 제공한다.
  dayjs.locale('ko');

  const {isAuthentication, user, isLoading} = useAuth();
  const [isReplyViewOpen, setIsReplyViewOpen] = useState(false)
  const [isReplyOpen, setIsReplyOpen] = useState(false)
  const [replyComment, setReplyComment] = useState({
    postId : postId,
    parentCommentId : parentCommentId ? parentCommentId : commentId,
    username : "",
    comment : ""
  });
  const replyEditorRef = useRef(null);
  const commentRef = useRef(null);
  const nav = useNavigate();
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;


  // const now = dayjs.utc();
  // // const commentWrittenDate = dayjs(writtenDate);
  // const commentWrittenDate = dayjs(writtenDate)

  const now = dayjs().utc();  // local
  const commentWrittenDate = dayjs(writtenDate);


  console.log(commentId);
  console.log("원본:", writtenDate);
  console.log("파싱:", dayjs(writtenDate).format());
  console.log("파싱 UTC:", dayjs.utc(writtenDate).format());
  console.log("로컬:", dayjs(writtenDate).local().format());
  console.log("now UTC:", dayjs.utc().format());

  console.log("test:"+ now.format());
  console.log("commentWrittenDate"+ now.format());


  useEffect(() => {
    if (isLoading) return ; // 인증

    if (user) {
      setReplyComment({...replyComment, username : user.username})
    }

  }, [isLoading]);

  useEffect(() => {
    if (!comment) return;

    findMention(comment);
  }, [comment]);


  useEffect(() => {
    // 알림에서 이동할때 대댓글인 경우
    // 대댓글 열기;
    if (!targetCommentIdForScroll) return;

    const targetCommentId = targetCommentIdForScroll.substring(8);

    for (const childComment of childComments) {
      if(childComment.commentId.toString() === targetCommentId) {
        setIsReplyViewOpen(true);
      }
    }

  }, [targetCommentIdForScroll]);


  const handleReplyEditorInput = (e) => {
    setReplyComment({...replyComment, comment : e.target.innerText});
  }

  const exceptCustomUserMention = (text) => {
    // 사용자가 수동으로 작성한 `{@닉네임}` 같은 형태는 escape 처리
    return text.replace(/\{@([\w가-힣]+)\}/g, '@$1'); // 그냥 평문으로 바꿈
  };


  const onReplySubmitButtonClick = () => {

    const editor = replyEditorRef.current;
    // 복사해서 원본 손상 안 되게
    const clone = editor.cloneNode(true);

    //사용자 수동 입력된 {@닉네임} → 평문으로 치환
    clone.innerHTML = exceptCustomUserMention(clone.innerHTML);

    // mention span 찾기
    const mentionSpan = clone.querySelector(".mention");

    let mentionText = "";
    if (mentionSpan) {
      mentionText = mentionSpan.innerText.trim(); // @홍길동
      mentionSpan.outerHTML = `{@${mentionText.substring(1)}}`; // → {@홍길동}
    }

    const finalComment = clone.innerText; // 최종 텍스트 추출

    const newReplyComment = {
      ...replyComment,
      comment: finalComment,
      mentionCommentId: commentId,
      mentionNickname: commentAuthor,
    };

    onClickCommentCreate(newReplyComment, setReplyComment);
    setIsReplyOpen(false);
    if (!parentCommentId) setIsReplyViewOpen(true);
  };

  const findMention = (text) => {

    if (!text) return;
    const regex = /\{@([\w가-힣]+)\}/;
    const match = text.match(regex);

    if (match) {
      const username = match[1];
      const replaceText = text.replace(
          regex,
          `<span id="mention${commentId}" class="mention" contenteditable="false">@${username} </span>`
      );

      commentRef.current.innerHTML = replaceText;
    }
  }

  //끝으로 이동
  const moveCursorToEnd = (element) => {
    const range = document.createRange();
    const selection = window.getSelection();

    // span 끝에 커서를 놓기
    range.selectNodeContents(element);
    range.collapse(false); // false = 끝으로 이동

    selection.removeAllRanges();
    selection.addRange(range);
  }

  //해당 유저 페이지 이동
  const moveTargetPage = () => {
    nav(`/myPage/${commentUsername}`,{
      state:{
        nickname : commentAuthor
      }
    })
  }



  return(
      <>
        <div id={`comment${commentId}`} className={"post_comment"}>
          <div className={"post_comment_header"}>
            <div className={"post_comment_header_left"}>
              <UserImg
                  radius={40}
                  nickname={commentAuthor}
                  userImg={commentUserImg && commentUserImg.fileId ? `${backendBaseUrl}/api/inlineFile/${commentUserImg.fileId}` : ""}
                  onClickImg={moveTargetPage}
              />
              <div className={"post_comment_header_left_right"}>
                <div
                    className={"comment_author"}
                    onClick={()=>moveTargetPage()}
                >
                  {commentAuthor}
                </div>
                <div className={"comment_date"}>{commentWrittenDate.from(now)}</div>
              </div>
            </div>
            {isAuthentication && user.username === commentUsername ?(
                <button
                    className={"comment_delete_button"}
                    onClick={()=>onClickCommentDelete(commentId)}
                >
                  삭제</button>
            ): ""}

          </div>
          <div ref={commentRef} className={"post_comment_body"}>
            {comment}
          </div>
          <div className={"post_comment_reply_section"}>
            <div className={"post_comment_reply_button_section"}>
              {childComments && childComments.length > 0 ?
                  (
                    <div className={"post_comment_reply_view_button"}
                         onClick={()=>setIsReplyViewOpen(!isReplyViewOpen)}
                    >
                      {/*∨ 답글 1개 ∧ 답글 1개*/}
                      {isReplyViewOpen ? `∧ 답글 ${childComments.length}개` : `∨ 답글 ${childComments.length}개`}
                    </div>
                  ) : null
              }
              <div className={"post_comment_reply_button"}
                   onClick={()=>{
                     setIsReplyOpen(!isReplyOpen)
                     if (!isReplyOpen && isAuthentication && parentCommentId && commentUsername !== user.username) {
                       setReplyComment({...replyComment, comment:`@${commentAuthor} `});

                       setTimeout(() => {
                         replyEditorRef.current.innerHTML = `<span id="mention${commentId}" class="mention" contenteditable="false">@${commentAuthor}&nbsp;</span>`
                       }, 0);

                     }

                     setTimeout(() => {
                       moveCursorToEnd(replyEditorRef.current);
                     }, 0);
                   }}
              >
                {isReplyOpen ? "- 답글달기" : "+ 답글달기"}
              </div>
            </div>

            {isReplyOpen && (
                <div className={"post_comment_reply_input_section"}>
                  <div
                      ref={replyEditorRef}
                      className={"reply-editor-display"}
                      contentEditable
                      onInput={handleReplyEditorInput}
                      placeholder={"댓글을 작성해주세요"}
                  />

                  {/*<textarea*/}
                  {/*    name={"comment"}*/}
                  {/*    onChange={onCommentChange(replyComment, setReplyComment)}*/}
                  {/*    value={replyComment.comment}*/}
                  {/*    ref={replyInputRef}*/}
                  {/*    placeholder="댓글을 작성해주세요."*/}
                  {/*    rows={4}*/}
                  {/*/>*/}
                  <Button2
                      buttonText={"작성하기"}
                      buttonEvent={onReplySubmitButtonClick}
                  />
                </div>
            )}

            {isReplyViewOpen && (
                <div className={"post_comment_reply_comment_section"}>
                  {childComments && childComments.map(childComment => {
                    return (
                        <div key={childComment.commentId} className={"post_comment_reply_comment"}>
                          <PostComment
                              key={childComment.commentId}
                              commentId={childComment.commentId}
                              commentAuthor={childComment.nickname}
                              writtenDate={childComment.createdDate}
                              onClickCommentDelete={onClickCommentDelete}
                              comment={childComment.comment}
                              onClickCommentCreate={onClickCommentCreate}
                              onCommentChange={onCommentChange}
                              postId={postId}
                              parentCommentId={commentId}
                              commentUsername={childComment.username}
                              commentUserImg={childComment.userImg}
                          />
                        </div>
                    )
                  })}
                </div>
            )}
          </div>
        </div>
      </>
  )
}

export default PostComment
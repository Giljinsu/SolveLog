import './PostEditor.css';
import {use, useCallback, useEffect, useRef, useState} from 'react';
import {Button2} from "../common/Button.jsx";
import {useNavigate} from "react-router-dom";
import {useSearchContext} from "../../context/SearchContext.jsx";
import useCategoryList from "../../hooks/useCategoryList.jsx";
import {useAuth} from "../../context/AuthContext.jsx";
import Tags from '@yaireo/tagify/react' // React-wrapper file
import '@yaireo/tagify/dist/tagify.css';
import axios from "../../context/axiosInstance.js";
import MarkdownRenderer from "../common/MarkdownRenderer.jsx";

const PostEditor = ({createPost, alarmList, setAlarmList, alarmId, closeAlarm,
  postDetail, updatePost, isTempButtonVisible, createAlarm}) => {
  const [postId, setPostId] = useState('');
  const [title, setTitle] = useState('');
  const [tags, setTags] = useState('');
  const [category, setCategory] = useState('');
  const [content, setContent] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [summary, setSummary] = useState('');
  // const [alarmList, setAlarmList] = useState('');
  const isSaveRef = useRef(false) // 저장인지 여부
  const nav = useNavigate();
  const {resetSearchCondition} = useSearchContext();
  const {categoryList} = useCategoryList("SEARCH_CATEGORY");
  const {user, isAuthentication, isLoading} = useAuth();


  const textAreaRef = useRef();

  // Tags의 value 를 , 로 구분하여 String
  const onTagChange = (e) => {
    const tagData = e.detail.tagify.value;

    const stringValue = tagData.map(tag => tag.value).join(',');

    setTags(stringValue);
  }

  // 이미지업로드 및 mdImg
  const uploadImage = async (file, isThumbnail) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("username", user.username)
    formData.append("isThumbnail", isThumbnail);
    if (postId) formData.append("postId", postId);

    try {
      return await axios.post("/api/uploadFile",formData);

    } catch (e) {
      // alert("이미지 업로드 실패");
      setAlarmList([
        ...alarmList,
        {
          id: alarmId.current++,
          content: "이미지 업로드 실패",
          type: "bad"
        }
      ]);
    }
  }

  //이미지 복사 붙혀넣기
  const onImagePaste = async (e) => {
    const items = e.clipboardData?.items;

    if (!items) return;

    for (const item of items) {

      if (item.type.indexOf("image") === 0) {
        const file = item.getAsFile();
        if (!file) return;

        const res = await uploadImage(file, "false");


        const fileId = res.data.fileId;

        // 백엔드 url
        //.env 파일에 서버 주소 저장
        const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

        const mdImage = `![](${backendBaseUrl}/api/inlineFile/${fileId})\n`;

        insertAtCursor(textAreaRef.current, mdImage)

      }
    }
  }

  // 커서 위치에 넣기
  const insertAtCursor = (textarea, text) => {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const value = textarea.value;

    const updateValue = value.substring(0, start) + text + value.substring(end);

    setContent(updateValue);

  }

  // 자동 요약 버튼 클리 시
  const onClickAutoSummaryButton = () => {
    // if (!content) alert("내용을 입력해주세요!");
    if (!content) setAlarmList([
        ...alarmList,
      {
        id: alarmId.current++,
        content: "내용을 입력해주세요!",
        type: "bad"
      }
      ]);

    // console.log("")
    // console.log(cleanMarkdown(content));

    setSummary(cleanMarkdown(content).slice(0,100));
  }

  // md 텍스트 순수 텍스트 변환
  const cleanMarkdown = (md) => {
    return md
    // 1. 이미지 제거 ![alt](url)
    .replace(/!\[.*?\]\(.*?\)/g, "")
    // 2. 코드블럭 제거 ```...``` 포함내용
    .replace(/```[\s\S]*?```/g, "")
    // 3. 인라인 코드 제거 `code`
    .replace(/`[^`]*`/g, "")
    // 4. 링크 텍스트만 남기기 [text](url) → text
    .replace(/\[([^\]]+)\]\((.*?)\)/g, "$1")
    // 5. 헤더/인용/리스트 마크다운 문자 제거
    .replace(/^>+|\*+|#+|\-+|_+|=+/gm, "")
    // 6. 줄바꿈 → 공백
    .replace(/\n+/g, " ")
    // 7. 여백 정리
    .replace(/\s{2,}/g, " ")
    // // 8. html <h1></h1> 태그 삭제
    // .replace(/<\/?(script|style)[^>]*>/gi, "")
    .trim();
  }

  // 썸네일 변경 시
  const onChangeThumbnail = async (e) => {
    const res = await uploadImage(e.target.files[0], "true");

    const fileId = res.data.fileId;

    // 백엔드 url
    //.env 파일에 서버 주소 저장
    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

    // const mdImage = `![](${backendBaseUrl}/api/inlineFile/${fileId})\n`;
    const mdImage = `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`

    setThumbnail({
      mdImage: mdImage,
      fileId: fileId,
      imageTitle: e.target.files[0].name,
    })
  }

  const deleteThumbnailFile = async (thumbnailId) => {
    try {
      await axios.post(`api/deleteFile/${thumbnailId}`);
    } catch (e) {
      console.log(e);
    }
  }


  useEffect(() => {

    return () => {
      const deleteTempFiles = async () => {
        try {
          await axios.post(`/api/deleteTempFiles/${user.username}`);
        } catch (e) {

        }
      }

      if (!isSaveRef.current) {
          deleteTempFiles();
        }
    }
  }, []);

  useEffect(() => {
    if (!postDetail) return;

    setPostId(postDetail.id);
    setTitle(postDetail.title);
    setContent(postDetail.content);
    setSummary(postDetail.summary);
    setCategory(postDetail.categoryType);
    setTags(postDetail.tags);

    const thumbnailFile = postDetail.files && postDetail.files.filter(file => {
      if (file.isThumbnail){
        return file.fileId;
      }
    });

    if (thumbnailFile) {
      const fileId = thumbnailFile[0].fileId;
      const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;
      const mdImage = `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`
      const imageTitle = thumbnailFile[0].originalFileName;

      setThumbnail({
        mdImage: mdImage,
        fileId: fileId,
        imageTitle: imageTitle,
      })
    }

  }, [postDetail]);

  return (
      <div className="post-editor-container">
        <h1 className="post-editor-title">글 작성하기</h1>

        <div className="editor-form">
          <div className="editor-left">
            <input
                name={"title"}
                type="text"
                placeholder="제목을 입력하세요."
                value={title || ""}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={60}
            />

            <div className={"editor-summary-section"}>
              <button className={"thumbnail-button"} onClick={onClickAutoSummaryButton}>
                자동요약
              </button>
              <input
                  name={"summary"}
                  type={"text"}
                  placeholder={"요약을 입력하세요"}
                  value={summary}
                  maxLength={100}
                  onChange={(e) => {
                    setSummary(e.target.value);
                  }}
              />
            </div>


            <div className={"editor-left-tags"}>
            <Tags
                  settings={{
                    delimiters: ",| ",   // 스페이스와 콤마를 태그 구분자로
                    maxTags: 5,
                    pattern: /^.{1,10}$/
                  }}
                  value={tags}
                  name={"tags"}
                  onChange={onTagChange}
                  placeholder='태그를 입력하세요.'
                  onInvalid={(e) => {
                    console.log("태그 생성 실패:", e.detail.data.value);
                    console.log("실패 사유:", e.detail.message);
                    console.log(e.detail);
                    // 예) "pattern mismatch", "duplicate", "maxTags exceeded"
                    //already exists 이미 존
                    //pattern mismatch 글자수 제한
                    //number of tags exceeded: 태그 최대 수 초과

                    switch (e.detail.message) {
                      case "already exists":
                        createAlarm("태그가 이미 존재합니다.", "bad");
                        break;
                      case "pattern mismatch":
                        createAlarm("태그는 최대 10자까지 입력 가능합니다.", "bad");
                        break;
                      case "number of tags exceeded":
                        createAlarm("태그는 최대 5개까지 생성 가능합니다.", "bad");
                        break;
                    }
                  }}
              />
            </div>

            <select
                name={"category"}
                value={category}
                onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">카테고리 선택</option>
              {categoryList && categoryList.map(category => (
                  <option
                      key={category.categoryId}
                      value={category.type}>{category.type}</option>
              ))}
              {/*<option value="algorithm">문제풀이</option>*/}
              {/*<option value="free">자유게시판</option>*/}
            </select>

            <div className={"thumbnail-button-section"}>
              <label className={"thumbnail-button"} htmlFor={"input_thumbnail"}>썸네일 업로드</label>
              <div>{thumbnail ? thumbnail.imageTitle : 'none'}</div>
            </div>

            <input type={"file"}
                   id={"input_thumbnail"}
                   accept={"image/jpeg, image/png, image/gif, image/bmp, image/webp"}
                   style={{display:"none"}}
                   onChange={(e) => {
                     if (thumbnail) deleteThumbnailFile(thumbnail.fileId)
                     onChangeThumbnail(e);
                   }}
            />

            <textarea
                name={"content"}
                ref={textAreaRef}
                placeholder="당신의 이야기를 적어보세요..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
                onPaste={onImagePaste}
            />

            <div className={"post-editor-save-btn"}>
              <Button2
                  buttonText={"뒤로가기"}
                  buttonEvent={() => {
                    nav("/");
                    resetSearchCondition();
                  }}
              />
              <div className={"post-editor-save-btn-right"}>
                {
                  // postDetail.isTemp === false || !postDetail.isTemp && (
                    isTempButtonVisible === true || (isTempButtonVisible !== "" && isTempButtonVisible !== false) && (
                    <Button2
                      buttonText={"임시작성"}
                      buttonEvent={() => {
                        isSaveRef.current = true;

                        !postId ? createPost({
                          username: user.username,
                          title: title || "",
                          tags: tags || "",
                          categoryType: category || "",
                          content: content|| "",
                          summary: summary || "",
                          isTemp: true
                        }) : updatePost({
                          postId: postId || "",
                          username: user.username,
                          title: title || "",
                          tags: tags || "",
                          categoryType: category || "",
                          content: content || "",
                          summary: summary || "",
                          isTemp: true
                        });
                      }}
                    />
                    )
                }
                <Button2
                    buttonText={"작성하기"}
                    buttonEvent={() => {
                      isSaveRef.current = true;

                      !postId ? createPost({
                        username: user.username,
                        title: title,
                        tags: tags,
                        categoryType: category,
                        content: content,
                        summary: summary,
                        isTemp: false
                      }) : updatePost({
                        postId: postId,
                        username: user.username,
                        title: title,
                        tags: tags,
                        categoryType: category,
                        content: content,
                        summary: summary,
                        isTemp: false
                      });

                    }}
                />
              </div>
            </div>
          </div>

          <div className="editor-preview-container">
            <div className="markdown-preview">
              <MarkdownRenderer content={`${thumbnail && thumbnail.mdImage}${content}`}/>
            </div>
          </div>

          <div className={"editor-alarm-list"}>

            {/*<div className={"editor-alarm editor-alarm-positive"}>*/}
            {/*  <div className={"editor-alarm-exit-button"}>x</div>*/}
            {/*  positive*/}
            {/*</div>*/}

            {/*<div className={"editor-alarm editor-alarm-bad"}>*/}
            {/*  <div className={"editor-alarm-exit-button"}>x</div>*/}
            {/*  bad*/}
            {/*</div>*/}
            { alarmList && alarmList.map(alarm =>
                (
                    <div key={alarm.id} className={`editor-alarm editor-alarm-${alarm.type}`}>
                      <div className={"editor-alarm-exit-button"}
                           onClick={() => closeAlarm(alarm.id)}
                      >x</div>
                      {alarm.content}
                    </div>
                )
            )}
          </div>

        </div>

      </div>
  );
};

export default PostEditor;
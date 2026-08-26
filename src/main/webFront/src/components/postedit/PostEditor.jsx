import './PostEditor.css';
import {use, useCallback, useEffect, useRef, useState} from 'react';
import {Button2} from "../common/Button.jsx";
import {useNavigate} from "react-router-dom";
import {useSearchContext} from "../../context/SearchContext.jsx";
import useCategoryList from "../../hooks/useCategoryList.jsx";
import {useAuth} from "../../context/AuthContext.jsx";
import {usePopup} from "../../context/PopupContext.jsx";
import Tags from '@yaireo/tagify/react' // React-wrapper file
import '@yaireo/tagify/dist/tagify.css';
import axios from "../../context/axiosInstance.js";
import MarkdownRenderer from "../common/MarkdownRenderer.jsx";
import AiPostGenerateModal from "./AiPostGenerateModal.jsx";
import usePostEditorLock from "../../hooks/usePostEditorLock.js";

const PostEditor = ({createPost, alarmList, setAlarmList, alarmId, closeAlarm,
  postDetail, updatePost, isTempButtonVisible, createAlarm}) => {
  const [postId, setPostId] = useState('');
  const [title, setTitle] = useState('');
  const [tags, setTags] = useState('');
  const [category, setCategory] = useState('');
  const [content, setContent] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [summary, setSummary] = useState('');
  const [isAiModalOpen, setIsAiModalOpen] = useState(false);
  const [isGeneratingThumbnail, setIsGeneratingThumbnail] = useState(false);
  const [thumbnailJobStatus, setThumbnailJobStatus] = useState('');
  const [isUploadingThumbnail, setIsUploadingThumbnail] = useState(false);
  // const [alarmList, setAlarmList] = useState('');
  const isSaveRef = useRef(false) // 저장인지 여부
  const prevThumbnailId = useRef('');
  const isThumbnailChangedRef = useRef(false); // 썸네일 변경 여부
  const thumbnailPollTimeoutRef = useRef(null);
  const nav = useNavigate();
  const {resetSearchCondition} = useSearchContext();
  const {categoryList} = useCategoryList("SEARCH_CATEGORY");
  const {user, isAuthentication, isLoading, canUseAi} = useAuth();
  const confirm = usePopup();
  const {isLockOwner, retryLock} = usePostEditorLock();


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
    // if (postId) formData.append("postId", postId);
    //수정이어도 추가하면 무조건 temp 파일로 하기위해 위의 내용을 주석처리

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

    // const updateValue = value.substring(0, start) + text + value.substring(end);
    //
    // setContent(updateValue);

    // 이 API가 직접 textarea.value를 수정하고, 커서/선택 영역도 관리해 줌
    textarea.setRangeText(text, start, end, 'end'); // 'end' → 삽입된 텍스트 뒤로 커서 이동

    setContent(textarea.value);

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
    // AI 생성 중이거나 이미 업로드가 진행 중이면 무시 (race condition 방지)
    // - UI에서 input/label을 disabled 처리해도, 프로그램적 트리거 등을 대비해 handler에서도 방어
    if (isGeneratingThumbnail || isUploadingThumbnail) {
      e.target.value = '';
      return;
    }

    const file = e.target.files[0];
    if (!file) return;

    setIsUploadingThumbnail(true);
    try {
      if (prevThumbnailId.current !== '') {
        deleteThumbnailFile(prevThumbnailId.current);
      }
      const res = await uploadImage(file, "true");

      const fileId = res.data.fileId;

      isThumbnailChangedRef.current = true;
      prevThumbnailId.current = fileId;
      // 백엔드 url
      //.env 파일에 서버 주소 저장
      const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

      // const mdImage = `![](${backendBaseUrl}/api/inlineFile/${fileId})\n`;
      const mdImage = `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`

      setThumbnail({
        mdImage: mdImage,
        fileId: fileId,
        imageTitle: file.name,
      })
    } finally {
      setIsUploadingThumbnail(false);
    }
  }

  const deleteThumbnailFile = async (thumbnailId) => {
    try {
      await axios.post(`/api/deleteFile/${thumbnailId}`);
    } catch (e) {
      console.log(e);
    }
  }

  const THUMBNAIL_JOB_POLL_INTERVAL_MS = 1500;
  const THUMBNAIL_JOB_MAX_WAIT_MS = 3 * 60 * 1000; // 3분

  const THUMBNAIL_JOB_STATUS_LABEL = {
    QUEUED: 'AI 썸네일 생성 대기 중...',
    PROCESSING: 'AI 썸네일 그리는 중...',
  };

  const stopThumbnailPolling = () => {
    if (thumbnailPollTimeoutRef.current) {
      clearTimeout(thumbnailPollTimeoutRef.current);
      thumbnailPollTimeoutRef.current = null;
    }
  }

  // Worker가 생성한 썸네일을 현재 form state에 반영 (수동 업로드와 동일한 방식 재사용)
  // - postId=null인 temp 파일이므로, 게시글 저장 시 기존 fileService.updateFilePostId()가
  //   username 기준으로 자동 연결한다. 별도로 fileId를 저장 요청에 실어 보낼 필요가 없다.
  const applyGeneratedThumbnail = (fileId) => {
    if (prevThumbnailId.current !== '') {
      deleteThumbnailFile(prevThumbnailId.current);
    }

    isThumbnailChangedRef.current = true;
    prevThumbnailId.current = fileId;

    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;
    const mdImage = `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`;

    setThumbnail({
      mdImage: mdImage,
      fileId: fileId,
      imageTitle: 'AI 생성 썸네일',
    });
  }

  const finishThumbnailGeneration = () => {
    stopThumbnailPolling();
    setIsGeneratingThumbnail(false);
    setThumbnailJobStatus('');
  }

  // jobId를 주기적으로 GET하여 QUEUED/PROCESSING -> COMPLETED/FAILED까지 대기
  const pollThumbnailJob = (jobId, startedAt) => {
    const checkStatus = async () => {
      let res;
      try {
        res = await axios.get(`/api/thumbnail-jobs/${jobId}`);
      } catch {
        finishThumbnailGeneration();
        createAlarm('썸네일 상태 확인에 실패했습니다. 다시 시도해 주세요.', 'bad');
        return;
      }

      const {status, fileId, errorMessage} = res.data;

      if (status === 'COMPLETED') {
        finishThumbnailGeneration();
        applyGeneratedThumbnail(fileId);
        createAlarm('AI 썸네일이 생성되었습니다.', 'positive');
        return;
      }

      if (status === 'FAILED') {
        finishThumbnailGeneration();
        createAlarm(errorMessage || '썸네일 생성에 실패했습니다. 다시 시도해 주세요.', 'bad');
        return;
      }

      // QUEUED / PROCESSING -> 계속 대기
      if (Date.now() - startedAt > THUMBNAIL_JOB_MAX_WAIT_MS) {
        finishThumbnailGeneration();
        createAlarm('썸네일 생성이 너무 오래 걸립니다. 다시 시도해 주세요.', 'bad');
        return;
      }

      setThumbnailJobStatus(status);
      thumbnailPollTimeoutRef.current = setTimeout(checkStatus, THUMBNAIL_JOB_POLL_INTERVAL_MS);
    }

    checkStatus();
  }

  // AI 썸네일 생성 버튼 클릭 시
  const onClickGenerateThumbnail = async () => {
    // 중복 클릭 방지 + 수동 업로드가 진행 중이면 함께 진행되지 않도록 방지 (race condition 방지)
    if (isGeneratingThumbnail || isUploadingThumbnail) return;

    if (!content || content.trim() === '') {
      createAlarm('썸네일을 생성하려면 먼저 내용을 입력해주세요.', 'bad');
      return;
    }

    // 혹시 남아있는 이전 polling이 있다면 정리 후 새로 시작 (중복 polling 방지)
    stopThumbnailPolling();
    setIsGeneratingThumbnail(true);
    setThumbnailJobStatus('QUEUED');

    try {
      const res = await axios.post('/api/thumbnail-jobs', {
        problemTitle: (title || '').slice(0, 100),
        problemDescription: content.slice(0, 5000),
      });

      pollThumbnailJob(res.data.jobId, Date.now());
    } catch (e) {
      finishThumbnailGeneration();
      const message = e.response?.data?.message
          || '썸네일 생성에 실패했습니다. 다시 시도해 주세요.';
      createAlarm(message, 'bad');
    }
  }

  const onTabKeyDown = (event) => {
    if(event.keyCode===9) {
      event.preventDefault();
      insertAtCursor(textAreaRef.current, '  ')
    }
  }

  // AI로 작성 버튼 클릭 시
  const onClickAiGenerateButton = async () => {
    if (content && content.trim() !== '') {
      const confirmed = await confirm({
        header: "AI 초안 생성",
        body: "현재 작성 중인 내용이 AI 생성 결과로 변경됩니다.\n계속하시겠습니까?",
        leftButtonText: "아니요",
        rightButtonText: "예"
      });
      if (!confirmed) return;
    }

    setIsAiModalOpen(true);
  }

  // AI 초안 생성 완료 시
  const onAiPostGenerated = (generatedMarkdown) => {
    setContent(generatedMarkdown);
    createAlarm("AI 초안이 생성되었습니다.", "positive");
  }

  const deleteTempFiles = async () => {
    try {
      await axios.post(`/api/deleteTempFiles/${user.username}`);
    } catch (e) {

    }
  }


  useEffect(() => {
    // lock을 획득하지 못한(다른 탭이 사용 중인) 탭은 애초에 이 화면을 사용하지 않으므로
    // temp 파일을 건드리면 안 된다 - 다른 탭의 작업 중인 temp 파일을 삭제해버리는 것을 방지.
    if (!isLockOwner) return;

    // 혹시 남아있을 임시파일 삭제
    deleteTempFiles();

    return () => {

      if (!isSaveRef.current) {
          deleteTempFiles();
        }

      stopThumbnailPolling();
    }
  }, [isLockOwner]);

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

    // 썸네일 유무
    if (thumbnailFile && thumbnailFile.length > 0) {
      const fileId = thumbnailFile[0].fileId;
      const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;
      const mdImage = `<img alt="이미지 없음" class="md-thumbnail" src="${backendBaseUrl}/api/inlineFile/${fileId}" />\n\n`
      const imageTitle = thumbnailFile[0].originalFileName;

      prevThumbnailId.current = fileId;

      setThumbnail({
        mdImage: mdImage,
        fileId: fileId,
        imageTitle: imageTitle,
      })
    }

  }, [postDetail]);

  // 카테고리 문제풀이 일시 템플릿 추가
  useEffect(() => {
    if (category === "문제풀이" && content === "") {
      setContent("# 문제\n"
          + "문제\n"
          + "\n"
          + "# 입력\n"
          + "입력\n"
          + "\n"
          + "# 출력\n"
          + "출력\n"
          + "\n"
          + "<div style=\"display: flex; gap: 20px;\">\n"
          + "\n"
          + "  <div style=\"flex: 1;\">\n"
          + "    <h4>예제 입력 </h4>\n"
          + "    <pre><code>\n"
          + "입력 값\n"
          + "    </code></pre>\n"
          + "  </div>\n"
          + "\n"
          + "  <div style=\"flex: 1;\">\n"
          + "    <h4>예제 출력 </h4>\n"
          + "    <pre><code>\n"
          + "출력 값\n"
          + "    </code></pre>\n"
          + "  </div>\n"
          + "\n"
          + "</div>");
    }
  }, [category]);

  // 다른 탭이 이미 게시글 작성 화면을 사용 중이면 폼 자체를 렌더링하지 않고 차단 화면만 보여준다.
  if (!isLockOwner) {
    return (
        <div className="post-editor-container post-editor-locked">
          <h1 className="post-editor-title">글 작성하기</h1>
          <div className="post-editor-locked-message">
            <p>다른 탭에서 게시글을 작성 중입니다.</p>
            <p>기존 작성 페이지를 종료한 후 다시 시도해주세요.</p>
          </div>
          <div className="post-editor-locked-actions">
            <Button2
                buttonText={"다시 확인"}
                buttonEvent={retryLock}
            />
            <Button2
                buttonText={"돌아가기"}
                buttonEvent={() => {
                  nav("/");
                  resetSearchCondition();
                }}
            />
          </div>
        </div>
    );
  }

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
                    pattern: /^.{1,20}$/
                  }}
                  value={tags}
                  name={"tags"}
                  onChange={onTagChange}
                  placeholder='태그를 입력하세요.'
                  onInvalid={(e) => {
                    console.log("태그 생성 실패:", e.detail.data.value);
                    console.log("실패 사유:", e.detail.message);
                    console.log(e.detail);
                    // 예 "pattern mismatch", "duplicate", "maxTags exceeded"
                    //already exists 이미 존
                    //pattern mismatch 글자수 제한
                    //number of tags exceeded: 태그 최대 수 초과

                    switch (e.detail.message) {
                      case "already exists":
                        createAlarm("태그가 이미 존재합니다.", "bad");
                        break;
                      case "pattern mismatch":
                        createAlarm("태그는 최대 20자까지 입력 가능합니다.", "bad");
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
              <label
                  className={`thumbnail-button${isGeneratingThumbnail || isUploadingThumbnail ? ' thumbnail-button-disabled' : ''}`}
                  htmlFor={"input_thumbnail"}
              >
                썸네일 업로드
              </label>
              {canUseAi && (
                  <button
                      type={"button"}
                      className={"thumbnail-button"}
                      onClick={onClickGenerateThumbnail}
                      disabled={isGeneratingThumbnail || isUploadingThumbnail}
                  >
                    {isGeneratingThumbnail ? 'AI 썸네일 생성 중...' : 'AI 썸네일 생성'}
                  </button>
              )}
              <div>
                {isGeneratingThumbnail
                    ? (THUMBNAIL_JOB_STATUS_LABEL[thumbnailJobStatus] || 'AI 썸네일 생성 중...')
                    : isUploadingThumbnail
                        ? '썸네일 업로드 중...'
                        : (thumbnail ? thumbnail.imageTitle : 'none')}
              </div>
            </div>

            <input type={"file"}
                   id={"input_thumbnail"}
                   accept={"image/jpeg, image/png, image/gif, image/bmp, image/webp"}
                   style={{display:"none"}}
                   disabled={isGeneratingThumbnail || isUploadingThumbnail}
                   onChange={(e) => {
                     // if (thumbnail) deleteThumbnailFile(thumbnail.fileId)
                     onChangeThumbnail(e);
                   }}
            />

            <textarea
                name={"content"}
                ref={textAreaRef}
                placeholder="당신의 이야기를 적어보세요..."
                value={content}
                onKeyDown={e => onTabKeyDown(e)}
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
                {canUseAi && (
                    <Button2
                        buttonText={"AI로 작성"}
                        buttonEvent={onClickAiGenerateButton}
                    />
                )}
                {
                  // postDetail.isTemp === false || !postDetail.isTemp && (
                    isTempButtonVisible === true || (isTempButtonVisible !== "" && isTempButtonVisible !== false) && (
                    <Button2
                      buttonText={"임시작성"}
                      buttonEvent={() => {
                        isSaveRef.current = true;
                        
                        //기존 썸네일 삭제
//                         if (postId && isThumbnailChangedRef.current) {
//                           deleteThumbnailFile(prevThumbnailId.current);
//                         }

                        isThumbnailChangedRef.current = false;

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

                      //기존 썸네일 삭제
//                       if (postId && isThumbnailChangedRef.current) {
//                         deleteThumbnailFile(prevThumbnailId.current);
//                       }

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

        {isAiModalOpen && (
            <AiPostGenerateModal
                onClose={() => setIsAiModalOpen(false)}
                onGenerated={onAiPostGenerated}
                createAlarm={createAlarm}
            />
        )}

      </div>
  );
};

export default PostEditor;
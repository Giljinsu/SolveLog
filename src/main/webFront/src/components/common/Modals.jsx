import "./Modals.css"
import {Button2} from "./Button.jsx";
import {useEffect, useRef, useState} from "react";
import axios from "axios";
import axiosInstance from "../../context/axiosInstance.js";
import {useAuth} from "../../context/AuthContext.jsx";
import {useSearchContext} from "../../context/SearchContext.jsx";
import {useNavigate} from "react-router-dom";
import {useAlarmStore} from "../../hooks/useAlarmStore.js";

export const LoginModal = ({setIsLoginOpen}) => {
  const [inputInfo, setInputInfo] = useState({
    username : "",
    password : "",
    rePassword : "",
    nickName : "",
    authCode : ""
  });
  const [isLoginFailed, setIsLoginFailed] = useState( false);
  const [isPasswordValidate, setIsPasswordValidate] = useState(false);
  const [isRePasswordValidate, setIsRePasswordValidate] = useState(false);
  const [isNotValidate, setIsNotValidate] = useState(false);
  const [menuSelected, setMenuSelected] = useState("login");
  const [responseMessage, setResponseMessage] = useState("");
  const [isSendEmail, setIsSendEmail] = useState(false);
  const {reFetchUser} = useAuth();

  //로그인 인풋
  const setLoginInput = (e) => {
    const name = e.target.name;
    if (name === "username") {
      // e.target.value = e.target.value.replace(/[^a-z0-9_]/g, '');
    } else if (name === "password" || name==="rePassword") {
      e.target.value = e.target.value.replace(/[^A-Za-z0-9!@#$%^&*()_\-+=<>?{}[\]~]/g, '');
    } else if (name === "authCode") {
      e.target.value = e.target.value.replace(/[^0-9_]/g, '');
    }
    const value = e.target.value;


    setIsNotValidate(false);

    setInputInfo({...inputInfo, [name] : value})
  }

  // 제출시 회원가입 or 로그인
  const onSubmit = async (e) => {
    e.preventDefault(); // form submit 시 새로고침하는 경향이 있는데 이걸 막음
    try {
      // if (menuSelected === "signup" && !isSendEmail) {
      //   setResponseMessage("코드를 전송해주세요.");
      //   setIsLoginFailed(true);
      //   return;
      // }
      // if (menuSelected === "signup" && isSendEmail && inputInfo.authCode === "") {
      //   setResponseMessage("코드를 입력해주세요.");
      //   setIsLoginFailed(true);
      //   return;
      // }
      //아이디 비밀번호
      if (inputInfo.username === "" || inputInfo.password === "") {
        setIsLoginFailed(false);
        setIsNotValidate(true);
        return;
      }

      // 회원가입
      if(menuSelected === "signup") {


        //비밀번호 확인용
        if (inputInfo.password !== inputInfo.rePassword) {
          setIsNotValidate(true);
          setIsRePasswordValidate(true);
          return;
        }
        setIsRePasswordValidate(false);

        // 비밀번호 유효성 검사
        if (!checkPassword()) {
          setIsNotValidate(true);
          setIsPasswordValidate(true);
          return;
        }
        setIsPasswordValidate(false);

        // 닉네임 입력
        if (inputInfo.nickName === "") {
          setIsNotValidate(true);
          return;
        }

        await axios.post("/api/createUser",inputInfo);
      }

      // 로그인
      const axiosResponse = await axiosInstance.post("/api/login", inputInfo);
      localStorage.setItem("accessToken", axiosResponse.data.accessToken);
      localStorage.setItem("refreshToken", axiosResponse.data.refreshToken);

      await reFetchUser();

      setIsLoginOpen(false)
    } catch (e) {
      setIsLoginFailed(true)
      if (e.response) {
        const {status, data} = e.response;

        if (status === 423 && data.code === "LOCKED_USER") {
          setResponseMessage(data.message);
        } else if (status === 409 && data.code === "DUPLICATE_USERNAME") {
          setResponseMessage(data.message);
        } else if (status === 404 && data.code === "NOT_EXIST_USER") {
          setResponseMessage(data.message)
        } else if (status === 404 && data.code === "NOT_VALIDATE_EMAIL_CODE") {
          setResponseMessage(data.message)
        } else {
          setResponseMessage("이메일 또는 비밀번호가 잘못되었습니다.")
        }
      }
    }
  }

  const onClickSendEmail = async () => {
    if (inputInfo.username === "") {
      setResponseMessage("이메일을 입력해주세요.");
      setIsLoginFailed(true);
      return;
    }
    if (!checkEmail()) {
      setResponseMessage("잘못된 이메일 형식입니다.");
      setIsLoginFailed(true);
      return;
    }
    setIsSendEmail(true);
    setIsLoginFailed(false);
    try {
      await axiosInstance.post("/api/sendEmailCode", {
        email: inputInfo.username,
      });

      setResponseMessage("이메일 인증코드를 해당 메일에 보냈습니다.");
      setIsLoginFailed(true);
    } catch (e) {
        console.log(e);
    }

  }

  const checkEmail = () => {
     return inputInfo.username.match(
         '^[a-zA-Z0-9+\\-_.]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+$') !== null;
  }

  const checkPassword = () => {
    // 영문 숫자 조합 8자리 이상
    let reg = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,25}$/

    // 영문 숫자 특수기호 조합 8자리 이상
    // let reg = /^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,15}$/

    return inputInfo.password.match(reg) != null;
  }

  const onClickResetPassword = async () => {
    if (inputInfo.username === "") {
      setIsLoginFailed(true);
      setResponseMessage("이메일을 입력해주세요.");
      return;
    }

    try {
      await axiosInstance.post(`/api/sendResetPwEmail/${inputInfo.username}`)
      setIsLoginFailed(true);
      setResponseMessage("해당 이메일에 비밀번호 변경 메일이 전송 되었습니다.")
    } catch (e) {
      setIsLoginFailed(true)
      if (e.response) {
        const {status, data} = e.response;

        if (status === 404 && data.code === "NOT_EXIST_USER") {
          setResponseMessage(data.message)
        }ㅂ
      }
    }
  }

  useEffect(() => {
    setInputInfo({
      username : "",
      password : "",
      rePassword: "",
      nickName : "",
      authCode : ""
    })

    setIsSendEmail(false);
    setIsNotValidate(false)
    setIsLoginFailed(false);
  }, [menuSelected]);

  return(
      <div className="modal-backdrop" onClick={() => setIsLoginOpen(false)}>
        {/*
          React에서 클릭 이벤트는 **버블링(bubbling)**이 일어납니다:
          모달 안을 클릭해도, 이벤트가 부모인 modal-backdrop까지 올라가서 모달이 닫혀버리는 현상 발생
          그래서 e.stopPropagation() 이 필요함
        */}
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <h2>{menuSelected === "login" ? "로그인" : "회원가입"}</h2>
          <div className={"login_menu"}>
            <div
                className={`${menuSelected === "login" ? "menu_selected" : ""}`}
                onClick={() => setMenuSelected("login")}
            >로그인</div>
            <div
                className={`${menuSelected === "signup" ? "menu_selected " : ""}`}
                onClick={() => setMenuSelected("signup")}
            >회원가입</div>
          </div>
          <form onSubmit={onSubmit}>
            <div className={"modal-login"}>
              <div className={"email-section"}>
                <input
                    name={"username"}
                    value={inputInfo.username}
                    type="text"
                    placeholder="이메일"
                    maxLength={20}
                    autoComplete={menuSelected === "login" ? "username" : "new-username"}
                    onChange={setLoginInput}
              />
              {menuSelected === "signup" ? (
                  <Button2
                      buttonEvent={onClickSendEmail}
                      buttonText={`${isSendEmail ? "재전송":"코드 전송"}`}
                      buttonType={"button"}
                  />
              ) : ""}
            </div>
            {isSendEmail ? (
              <input
                  name={"authCode"}
                  placeholder={"코드를 입력해주세요."}
                  onChange={setLoginInput}
              />
            ) : ""}
            <input
                name={"password"}
                value={inputInfo.password}
                  type="password"
                  placeholder="비밀번호"
                  maxLength={30}
                  autoComplete={menuSelected === "login" ? "current-password" : "new-password"}
                  onChange={setLoginInput}
              />
              {menuSelected === "signup" ? (
                    <>
                      <input
                          name={"username"}
                          autoComplete="new-username"
                          style={{display: "none"}}
                      />
                      <input
                          name={"rePassword"}
                          value={inputInfo.rePassword}
                          type="password"
                          placeholder="비밀번호 재입력"
                          maxLength={30}
                          autoComplete="new-password"
                          onChange={setLoginInput}
                      />
                      <input
                          name={"nickName"}
                          value={inputInfo.nickName}
                          type="text"
                          placeholder="닉네임"
                          maxLength={15}
                          autoComplete="new-password"
                          onChange={setLoginInput}
                      />
                    </>
                ) : null}
              {isLoginFailed && (
                      <div className={"login_failed"}>
                        {responseMessage}
                      </div>
              )}
              {isNotValidate && (inputInfo.username === "" ? (
                      <div className={"login_failed"}>
                        이메일을 입력해주새요.
                      </div>
                  ) : inputInfo.password === "" ? (
                      <div className={"login_failed"}>
                        비밀번호를 입력해주세요.
                      </div>
                  ) : isRePasswordValidate ? (
                      <div className={"login_failed"}>
                        비밀번호가 일치하지 않습니다.
                      </div>
                  ) : isPasswordValidate ? (
                      <div className={"login_failed"}>
                        비밀번호는 영문자, 숫자를 포함한 8자 이상이어야 합니다.
                      </div>
                  ) : inputInfo.nickName === "" && menuSelected === "signup" ? (
                      <div className={"login_failed"}>
                        닉네임을 입력해주세요.
                      </div>
                  ) : null)}
              {menuSelected === "login" &&
                <div className={"find-pw-button-section"}>
                  <div
                      className={"find-pw-button"}
                      onClick={onClickResetPassword}
                  >
                    비밀번호 찾기
                  </div>
                </div>
              }

            </div>
            <div className={"modal-content-bottom"}>
              <Button2
                  // buttonEvent={onLogin}
                  buttonText={`${menuSelected === "login" ? "로그인" : "회원가입"}`}
                  buttonType={"submit"}
              />
            </div>
          </form>
        </div>
      </div>
  )
}

// 디바운스 (시간제한)
function useDebounced(value, delay=300) {
  const [v, setV] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setV(value), delay);
    return () => clearTimeout(id); // value 가 변경되면 타이머 삭제
  }, [value, delay]);
  return v;
}

//검색 창 모달
export const SearchModal = ({setIsSearchOpen}) => {
  const {searchCondition, setSearchInput} = useSearchContext();
  const [searchValue, setSearchValue] = useState("")
  const [autoCompleteList, setAutoCompleteList] = useState([]);
  const [tag, setTag] = useState("");
  const debounced = useDebounced(tag);
  const abortRef = useRef(null); // 이전요청 취소
  const lastRef = useRef(""); // 이전값 요청

  //검색입력값 변경 시
  const onSearchValueChanged = (e) => {
    const changed = e.target.value;
    setSearchValue(changed);

    if (changed.slice(changed.length-1) === "#") {
      setAutoCompleteList([]);
      lastRef.current = ""
      return;
    }

    const tags = changed.match(/#\S*/g);
    if (tags && tags.length>0) {
      setTag(tags[tags.length-1]);
    } else {
      setAutoCompleteList([]);
      setTag("");
    }
    // getTagAutoComplete(changed);
  }


  //검색 버튼 클릭시
  const onSearchButtonClicked = () => {
    if (searchValue === "") return;
    setSearchInput("searchValue",searchValue);
    setIsSearchOpen(false);
  }

  //엔터키 클릭 시
  const onKeyDown = (e) => {
    if (e.keyCode === 13) { //엔터키
      onSearchButtonClicked();
    }
  }

  //태그 자동완성
  const getTagAutoComplete = async (value) => {
    try {
      const slice = value.slice(1);
      const axiosResponse = await axios.get(`/api/getTagAutoCompleteList/${slice}`);
      lastRef.current = value;
      setAutoCompleteList(axiosResponse.data)
    } catch (e) {
      
    }
  }

  // 자동완성 클릭
  const onClickSuggest = (suggest) => {

    const pureString = searchValue.match(/#\S*$/);
    // 마지막 태그 #을 제외한 순수 공백이아닌 문자 크기
    const pureStringLength = pureString[0].length-1;

    const removedText = searchValue.slice(0,searchValue.length - pureStringLength);
    setSearchValue(removedText + suggest);
  }

  useEffect(() => {
    const tag = debounced;

    if (tag.slice(0,1) !== "#") return;


    if (tag.length < 1 || tag === lastRef.current) return;


    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    getTagAutoComplete(tag);


    return ()=> controller.abort();
  }, [debounced]);


  return(
      <div
          className={"modal-backdrop"}
          onClick={() => setIsSearchOpen(false)}
      >

        <div className={"modal-content modal-content-search"}
             onClick={e => e.stopPropagation()}>

          <div className={"modal-search"}>
            <input
                type="text"
                placeholder="검색어를 입력해주세요."
                value={searchValue}
                onChange={onSearchValueChanged}
                onKeyDown={onKeyDown}
            />

            <button
                onClick={onSearchButtonClicked}>
              🔍
            </button>
          </div>
          {
            autoCompleteList && (
                <div className={"tag-auto-complete"}>
                  {autoCompleteList.map(ac => (
                    <div
                        key={ac.tagId}
                        className={"tag-auto-complete-item"}
                        onClick={()=>onClickSuggest(ac.tagName)}
                    >
                      {ac.tagName}
                    </div>
                  ))}
                </div>
              )
          }

        </div>
      </div>
  )
}

// 알림창 모달
export const AlarmModal = ({alarmList, getAlarm, deleteAlarm,
  updateAllAlarmIsViewed, deleteAllAlarm}) => {
  const nav = useNavigate();
  const {user} = useAuth();

  //알림 조회
  const viewAlarm = async (alarmId) => {
    try {
      await axiosInstance.post(`/api/viewAlarm/${alarmId}`)
      getAlarm(user.username);
    } catch (e) {
      console.log(e);
    }
  }

  // 알림클릭시
  const onClickAlarmItem = (alarm) => {
    nav(alarm.link, {
      state : {
        postId : alarm.postId,
        refreshKey: Date.now(),
      }
    })
  }



  return (
      <div className={"alarm-modal-section"}>
        <div className={"alarm-modal-header"}>
          <div>
            알림
          </div>
            {alarmList.length > 0 && (
              <div className={"alarm-modal-header-button-section"}>
                <button onClick={()=>updateAllAlarmIsViewed()}>전체읽기</button>
                <button onClick={()=>deleteAllAlarm()}>전체삭제</button>
              </div>
            )}
        </div>

        <div className={"alarm-modal-content"}>
          {alarmList.length > 0 ? alarmList.map(alarm =>
              (
                  <div key={alarm.alarmId} className={"alarm-modal-content-item"}>
                    <div className={`alarm-model-item-content ${alarm.isViewed ? "alarm-modal-item-content-viewed":""}`}
                         onClick={() => {
                           viewAlarm(alarm.alarmId);
                           onClickAlarmItem(alarm);
                         }
                    }>
                      {alarm.content}
                    </div>
                    <div
                        onClick={()=>deleteAlarm(alarm.alarmId)}
                        className={"alarm-model-item-button"}>
                      X
                    </div>
                  </div>
              )
          ) : <div className={"alarm-modal-no-items"}>알림이 없습니다..</div>}

        </div>

      </div>
  )
}

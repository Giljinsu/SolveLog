import "./Modals.css"
import {Button2} from "./Button.jsx";
import {useEffect, useRef, useState} from "react";
import axios from "axios";
import axiosInstance from "../../context/axiosInstance.js";
import {useAuth} from "../../context/AuthContext.jsx";
import {useSearchContext} from "../../context/SearchContext.jsx";
import {useNavigate} from "react-router-dom";

export const LoginModal = ({setIsLoginOpen}) => {
  const [inputInfo, setInputInfo] = useState({
    username : "",
    password : "",
    rePassword : "",
    nickName : ""
  });
  const [isLoginFailed, setIsLoginFailed] = useState( false);
  const [isRePasswordValidate, setIsRePasswordValidate] = useState(false);
  const [isNotValidate, setIsNotValidate] = useState(false);
  const [menuSelected, setMenuSelected] = useState("login");
  const [responseMessage, setResponseMessage] = useState("");
  const {reFetchUser} = useAuth();

  //로그인 인풋
  const setLoginInput = (e) => {
    const name = e.target.name;
    if (name === "username") {
      e.target.value = e.target.value.replace(/[^a-z0-9_]/g, '');
    } else if (name === "password" || name==="rePassword") {
      e.target.value = e.target.value.replace(/[^A-Za-z0-9!@#$%^&*()_\-+=<>?{}[\]~]/g, '');
    }
    const value = e.target.value;


    setIsNotValidate(false);

    setInputInfo({...inputInfo, [name] : value})
  }

  // 제출시 회원가입 or 로그인
  const onSubmit = async (e) => {
    e.preventDefault(); // form submit 시 새로고침하는 경향이 있는데 이걸 막음
    try {
      //아이디 비밀번호
      if (inputInfo.username === "" || inputInfo.password === "") {
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

        // 닉네임 입력
        if (inputInfo.nickName === "") {
          setIsNotValidate(true);
          return;
        }

        await axios.post("/api/createUser",inputInfo);
      }

      // 로그인
      const axiosResponse = await axios.post("/api/login", inputInfo);
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
        } else {
          setResponseMessage("아이디 또는 비밀번호가 잘못되었습니다.")
        }
      }
    }
  }

  useEffect(() => {
    setInputInfo({
      username : "",
      password : "",
      rePassword: "",
      nickName : ""
    })

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
              <input
                  name={"username"}
                  value={inputInfo.username}
                  type="text"
                  placeholder="아이디"
                  maxLength={20}
                  autoComplete={menuSelected === "login" ? "username" : "new-username"}
                  onChange={setLoginInput}
              />
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
                        아이디를 입력해주새요.
                      </div>
                  ) : inputInfo.password === "" ? (
                      <div className={"login_failed"}>
                        비밀번호를 입력해주세요.
                      </div>
                  ) : isRePasswordValidate ? (
                      <div className={"login_failed"}>
                        비밀번호가 일치하지 않습니다.
                      </div>
                  ) : inputInfo.nickName === "" && menuSelected === "signup" ? (
                      <div className={"login_failed"}>
                        닉네임을 입력해주세요.
                      </div>
                  ) : null)}

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

  //알림 조회
  const viewAlarm = async (alarmId) => {
    try {
      await axiosInstance.post(`/api/viewAlarm/${alarmId}`)
      getAlarm();
    } catch (e) {
      console.log(e);
    }
  }

  // 알림클릭시
  const onClickAlarmItem = (alarm) => {
    nav(alarm.link, {
      state : {
        postId : alarm.postId
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

import "./ResetPage.css"
import Title from "./common/Title.jsx";
import {Button2} from "./common/Button.jsx";
import {useSearchParams} from "react-router-dom";
import {useEffect, useState} from "react";
// import axios from "axios";
import axios from "../context/axiosInstance.js";

const ResetPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [isNotValidate, setIsNotValidate] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [inputInfo, setInputInfo] = useState({
    username : "",
    password : "",
    rePassword : "",
  });
  const [isFinished, setIsFinished] = useState(false) // 비밀번호 변경 완료

  //로그인 인풋
  const setLoginInput = (e) => {
    const name = e.target.name;
    const value = e.target.value;
    if (name === "password" || name==="rePassword") {
      e.target.value = e.target.value.replace(/[^A-Za-z0-9!@#$%^&*()_\-+=<>?{}[\]~]/g, '');
    }

    setIsNotValidate(false);

    setInputInfo({...inputInfo, [name] : value})
  }

  const getEmailByToken = async () => {
    try {
      const axiosResponse = await axios.post(`/api/getEmailByResetToken/${token}`);
      setInputInfo({username: axiosResponse.data.data.username, password: "", rePassword: ""});
    } catch (e) {
      console.log(e);
    }
  }

  const onSubmit = async () => {
    // 비밀번호 유효성 검사
    if (!checkPassword()) {
      return;
    }
    try {
      await axios.post("/api/resetPassword",{
        resetToken : token,
        username: inputInfo.username,
        password: inputInfo.password
      })

      setIsFinished(true);
    } catch (e) {
      console.log(e)
    }
  }

  const checkPassword = () => {
    if (inputInfo.password === "") {
      setIsNotValidate(true);
      setErrorMessage("비밀번호를 입력해 주세요.");
      return false;
    }

    //비밀번호 확인용
    if (inputInfo.password !== inputInfo.rePassword) {
      setIsNotValidate(true);
      setErrorMessage("비밀번호가 일치하지 않습니다.");
      return false;
    }

    // 영문 숫자 조합 8자리 이상
    let reg = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,25}$/

    // 영문 숫자 특수기호 조합 8자리 이상
    // let reg = /^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,15}$/

    if(inputInfo.password.match(reg) == null) {
      setIsNotValidate(true);
      setErrorMessage("비밀번호는 영문자, 숫자를 포함한 8자 이상이어야 합니다.");
      return false;
    }


    return inputInfo.password.match(reg) != null;
  }

  useEffect(() => {
    getEmailByToken();
  }, []);

  return (
      <div className={"reset-page-section"}>
        {inputInfo.username !== "" ? (
            <form className={"reset-page-content"}>
              <Title/>
              <input
                  className={"reset-page-email"}
                  value={inputInfo.username}
                  readOnly={true}
                  autoComplete="new-username"
                  name={"username"}
                  type={"text"}
              />
              <input
                  name={"password"}
                  type="password"
                  placeholder="새 비밀번호"
                  autoComplete="new-password"
                  maxLength={30}
                  value={inputInfo.password}
                  onChange={setLoginInput}
                  readOnly={isFinished && true}
              />
              <input
                  name={"rePassword"}
                  type="password"
                  placeholder="새 비밀번호 재입력"
                  autoComplete="new-password"
                  maxLength={30}
                  value={inputInfo.rePassword}
                  onChange={setLoginInput}
                  readOnly={isFinished && true}
              />
              {isNotValidate ? (
                  <div className={"reset-page-error-text"}>
                    {errorMessage}
                  </div>
              ) : (
                  ""
              )}
              {isFinished ? (
                <div className={"reset-page-finish"}>
                  비밀번호가 변경되었습니다. 다시 홈페이지로 돌아가 로그인해주시길 바랍니다.
                </div>
              ) : (
                <Button2
                    buttonText={"비밀번호 재설정"}
                    buttonType={"button"}
                    buttonEvent={onSubmit}
                />
              )}
            </form>
          ) : (
              <div className={"reset-page-content"}>
                <Title/>
                <div className={"reset-page-error"}>
                  만료된 요청입니다.
                </div>
              </div>
        )}

            </div>
        )
        }

        export default ResetPage;
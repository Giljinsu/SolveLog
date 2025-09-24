import './App.css'
import {Routes, Route} from "react-router-dom";
import {createContext, useState} from "react";
import Home from "./page/Home.jsx";
import Post from "./page/Post.jsx";
import {AuthProvider} from "./context/AuthContext.jsx"
import Signup from "./page/Signup.jsx";
import axios from "axios";
import {SearchProvider} from "./context/SearchContext.jsx";
import PostEdit from "./page/PostEdit.jsx";
import PostNew from "./page/PostEdit.jsx";
import 'highlight.js/styles/atom-one-dark.css';
import TempPost from "./page/TempPost.jsx";
import {PopupProvider} from "./context/PopupContext.jsx";
import MyPage from "./page/MyPage.jsx";

// export const PostStateContext = createContext();
// export const PostSDispatchContext = createContext();
  export const LoginContext = createContext();
  export const LoginDispatchContext = createContext();

  const getPostList = async () => {
    try {
      let api = await axios.get("/api/getPostList", searchCondition);
      setPostList(api.data.content);

    } catch (e) {
      console.log(e);
    }
  }

function App() {
  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  return (
      <>
        <SearchProvider>
          <AuthProvider>
            <PopupProvider>
                <LoginContext.Provider value={{isLoginOpen, isSearchOpen}}>
                  <LoginDispatchContext.Provider value={{
                    setIsLoginOpen,
                    setIsSearchOpen,
                  }}>
                    <Routes>
                      <Route path={"/"} element={<Home /> }></Route>
                      <Route path={"/post/:title"} element={<Post /> }></Route>
                      <Route path={"/signup"} element={<Signup />}></Route>
                      <Route path={"/postEdit"} element={<PostEdit />}></Route>
                      <Route path={"/tempPost"} element={<TempPost />}></Route>
                      <Route path={"/myPage/:username"} element={<MyPage />}></Route>
                      {/*<Route path={"/postNew"} element={<PostNew />}></Route>*/}
                    </Routes>
                  </LoginDispatchContext.Provider>
                </LoginContext.Provider>
            </PopupProvider>
          </AuthProvider>
        </SearchProvider>
      </>
  )
}

export default App

import "./Dropdown.css"
import {useContext, useEffect, useRef, useState} from "react";
import {options, getOptionsName} from "../../utils/SearchOptions.js"
import {useSearchContext} from "../../context/SearchContext.jsx";

const Dropdown = () => {
  const [open, setOpen] = useState(false);
  const menuRef = useRef();

  const {setSearchInput, setIsReady, searchCondition} = useSearchContext();
  const selected = searchCondition.searchOrderType || ''

  useEffect(() => {
    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);

    if (!searchCondition.searchOrderType) {
     setSearchInput("searchOrderType",options[0]);
    }

    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return(
      <div className="dropdown" ref={menuRef}>
        <button className="dropdown-btn" onClick={() => setOpen(!open)}>
          {getOptionsName(selected)} <span className="arrow">▾</span>
        </button>
        {open && (
            <ul className="dropdown-menu">
              {options.map((option) => (
                  <li
                      value={option}
                      key={option}
                      className={option === selected ? 'selected' : ''}
                      onClick={() => {
                        setOpen(false);
                        setSearchInput("searchOrderType",option)
                                              }}
                  >
                    {getOptionsName(option)}
                  </li>
              ))}
            </ul>
        )}
      </div>
  )
}

export default Dropdown;
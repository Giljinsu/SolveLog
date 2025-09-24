import "./Button.css"

export const Button1 = ({buttonText, buttonEvent}) => {
  return (
      <>
        <button className={"button1"}
            onClick={buttonEvent}
        >
          {buttonText}
        </button>
      </>
  )
}

export const Button2 = ({buttonText, buttonEvent, buttonType}) => {
  return (
      <>
        <button className={"button2"}
                type={buttonType}
                onClick={buttonEvent}
        >
          {buttonText}
        </button>
      </>
  )
}

// export default Button1;
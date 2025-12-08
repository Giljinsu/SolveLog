import "./MarkdownRenderer.css"
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import rehypeRaw from "rehype-raw";
import ReactMarkdown from "react-markdown";
import {memo, useMemo} from "react";

const MarkdownRenderer = ({content, headingRenderer}) => {
  // headingRenderer : 헤더들의 id 를 만들어줌

  const imgComponents = useMemo(() => ({
    img: ({node, ...props}) => {
      return(<img
          loading={"lazy"}
          alt={"no-image"}
          style={{maxWidth:'100%', display:'block', margin:'0 auto 3rem auto'}}
          {...props}
      />);
    }
  }), []);

  return headingRenderer ? (
      <div className={"markdown-body"}>
        <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeHighlight, rehypeRaw]}
            components={{
              h1 : headingRenderer("h1"),
              h2 : headingRenderer("h2"),
              h3 : headingRenderer("h3"),
              h4 : headingRenderer("h4"),
              h5 : headingRenderer("h5"),
              h6 : headingRenderer("h7"),
              img: ({node, ...props}) => {
                return(<img
                    loading={"lazy"}
                    alt={"no-image"}
                    style={{maxWidth:'100%', display:'block', margin:'0 auto 3rem auto'}}
                    {...props}
                />);
              }
              // imgComponents
            }}
        >
          {content}
        </ReactMarkdown>
      </div>
  ) : (
        <div className={"markdown-body"}>
          <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              rehypePlugins={[rehypeHighlight, rehypeRaw]}
              components={
                // {
                //   img: ({node, ...props}) => {
                //     return(<img
                //         loading={"lazy"}
                //         style={{maxWidth:'100%', display:'block', margin:'0 auto 3rem auto'}}
                //         {...props}
                //     />);
                //   },
                // }
                imgComponents
              }
          >
            {content}
          </ReactMarkdown>
        </div>
        )


        }

        export default memo(MarkdownRenderer);
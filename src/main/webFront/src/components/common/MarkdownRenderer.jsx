import "./MarkdownRenderer.css";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import rehypeRaw from "rehype-raw";
import ReactMarkdown from "react-markdown";
import mermaid from "mermaid";
import { memo, useEffect, useMemo, useRef, useState } from "react";

mermaid.initialize({
  startOnLoad: false,
  securityLevel: "loose",
});

const MermaidBlock = memo(({ code }) => {
  const idRef = useRef(`mermaid-${Math.random().toString(36).slice(2)}`);
  const [svg, setSvg] = useState("");
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const render = async () => {
      const text = code.trim();

      if (!text) {
        setSvg("");
        setError(false);
        return;
      }

      // 입력 중인 상태에서는 mermaid로 렌더하지 않고 코드블록 그대로 보여주기
      const looksRenderable =
        /^(graph|flowchart|sequenceDiagram|classDiagram|stateDiagram|erDiagram|journey|gantt|pie|mindmap|timeline|gitGraph|quadrantChart|requirementDiagram|C4Context|C4Container|C4Component|C4Dynamic|C4Deployment|architecture-beta)\b/m.test(
          text
        );

      if (!looksRenderable) {
        setSvg("");
        setError(true);
        return;
      }

      try {
        const { svg } = await mermaid.render(idRef.current, text);

        if (!cancelled) {
          setSvg(svg);
          setError(false);
        }
      } catch (e) {
        if (!cancelled) {
          setSvg("");
          setError(true);
        }
      }
    };

    render();

    return () => {
      cancelled = true;
    };
  }, [code]);

  if (!code.trim() || error || !svg) {
    return (
      <pre>
        <code className="language-mermaid">{code}</code>
      </pre>
    );
  }

  return (
    <div
      className="mermaid-block"
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  );
});

const MarkdownRenderer = ({ content, headingRenderer }) => {
  // headingRenderer : 헤더들의 id 를 만들어줌

  const imgComponents = useMemo(
    () => ({
      img: ({ node, ...props }) => {
        return (
          <img
            loading={"lazy"}
            alt={"no-image"}
            style={{ maxWidth: "100%", display: "block", margin: "0 auto 3rem auto" }}
            {...props}
          />
        );
      },
    }),
    []
  );

  const codeComponent = useMemo(() => {
    return ({ node, inline, className, children, ...props }) => {
      const match = /language-(\w+)/.exec(className || "");
      const code = String(children).replace(/\n$/, "");

      if (!inline && match?.[1] === "mermaid") {
        return <MermaidBlock code={code} />;
      }

      return (
        <code className={className} {...props}>
          {children}
        </code>
      );
    };
  }, []);

  return headingRenderer ? (
    <div className={"markdown-body"}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight, rehypeRaw]}
        components={{
          h1: headingRenderer("h1"),
          h2: headingRenderer("h2"),
          h3: headingRenderer("h3"),
          h4: headingRenderer("h4"),
          h5: headingRenderer("h5"),
          h6: headingRenderer("h6"),
          code: codeComponent,
          img: ({ node, ...props }) => {
            return (
              <img
                loading={"lazy"}
                alt={"no-image"}
                style={{ maxWidth: "100%", display: "block", margin: "0 auto 3rem auto" }}
                {...props}
              />
            );
          },
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
        components={{
          ...imgComponents,
          code: codeComponent,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default memo(MarkdownRenderer);
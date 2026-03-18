import React from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import { NoteContent } from '../../types';
import './NoteEditor.css';

interface Props {
  content: NoteContent;
  onChange: (content: NoteContent) => void;
}

const NoteEditor: React.FC<Props> = ({ content, onChange }) => {
  const editor = useEditor({
    extensions: [StarterKit, Image],
    content: content.body || '<p></p>',
    onUpdate: ({ editor }) => onChange({ body: editor.getHTML() }),
  });

  return (
    <div className="note-editor">
      <div className="note-toolbar">
        <button onClick={() => editor?.chain().focus().toggleBold().run()}
          className={editor?.isActive('bold') ? 'active' : ''}>B</button>
        <button onClick={() => editor?.chain().focus().toggleItalic().run()}
          className={editor?.isActive('italic') ? 'active' : ''}>I</button>
        <button onClick={() => editor?.chain().focus().toggleStrike().run()}
          className={editor?.isActive('strike') ? 'active' : ''}>S</button>
        <span className="toolbar-divider" />
        <button onClick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}>H1</button>
        <button onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}>H2</button>
        <span className="toolbar-divider" />
        <button onClick={() => editor?.chain().focus().toggleBulletList().run()}>•</button>
        <button onClick={() => editor?.chain().focus().toggleOrderedList().run()}>1.</button>
        <button onClick={() => editor?.chain().focus().toggleBlockquote().run()}>"</button>
        <button onClick={() => editor?.chain().focus().setHorizontalRule().run()}>—</button>
      </div>
      <EditorContent editor={editor} className="note-content" />
    </div>
  );
};

export default NoteEditor;

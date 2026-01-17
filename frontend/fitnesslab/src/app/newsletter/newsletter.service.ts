import { Injectable, signal } from '@angular/core';

export interface Newsletter {
  id: string;
  title: string;
  content: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface NewsletterTemplate {
  id: string;
  name: string;
  description: string;
  html: string;
}

@Injectable({
  providedIn: 'root'
})
export class NewsletterService {
  private newsletters = signal<Newsletter[]>([]);
  private templates = signal<NewsletterTemplate[]>(this.getDefaultTemplates());

  private getDefaultTemplates(): NewsletterTemplate[] {
    return [
      {
        id: 'blank',
        name: 'Blank',
        description: 'Start with a blank newsletter',
        html: '<p>Start writing your newsletter here...</p>'
      },
      {
        id: 'basic',
        name: 'Basic Newsletter',
        description: 'Simple newsletter with header and content sections',
        html: `<div style="max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;"><div style="background-color: #3b82f6; color: white; padding: 2rem; text-align: center;"><h1 style="margin: 0;">Your Company Name</h1></div><div style="padding: 2rem; background-color: #f8f9fa;"><h2>Newsletter Title</h2><p>Welcome to our newsletter! Add your content here.</p></div><div style="background-color: #e5e7eb; padding: 1rem; text-align: center; font-size: 0.875rem;"><p>&copy; 2024 Your Company. All rights reserved.</p></div></div>`
      },
      {
        id: 'promotional',
        name: 'Promotional',
        description: 'Eye-catching promotional newsletter',
        html: `<div style="max-width: 600px; margin: 0 auto; font-family: Helvetica, Arial, sans-serif;"><div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 3rem 2rem; text-align: center;"><h1 style="margin: 0; font-size: 2.5rem;">Special Offer!</h1><p style="font-size: 1.25rem; margin-top: 1rem;">Limited Time Only</p></div><div style="padding: 2rem; background-color: white;"><h2 style="color: #667eea;">Don't Miss Out!</h2><p style="font-size: 1.125rem;">Get amazing deals now.</p><div style="text-align: center; margin: 2rem 0;"><a href="#" style="display: inline-block; background-color: #667eea; color: white; padding: 1rem 2rem; text-decoration: none; border-radius: 0.5rem; font-weight: bold;">Shop Now</a></div></div></div>`
      },
      {
        id: 'professional',
        name: 'Professional',
        description: 'Clean professional template with header, content area, and footer',
        html: `<div style="max-width: 650px; margin: 0 auto; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #ffffff; box-shadow: 0 0 20px rgba(0,0,0,0.1);"><div style="background-color: #2c3e50; padding: 2.5rem 2rem; border-bottom: 4px solid #3498db;"><h1 style="margin: 0; color: #ffffff; font-size: 2rem; font-weight: 600; letter-spacing: 0.5px;">Your Newsletter Title</h1><p style="margin: 0.5rem 0 0 0; color: #ecf0f1; font-size: 0.9rem;">Monthly Update • January 2024</p></div><div style="padding: 3rem 2.5rem; background-color: #ffffff; line-height: 1.8; color: #2c3e50;"><h2 style="color: #2c3e50; font-size: 1.5rem; margin-top: 0; margin-bottom: 1.5rem; border-bottom: 2px solid #3498db; padding-bottom: 0.5rem;">Main Content Area</h2><p style="margin-bottom: 1.25rem; font-size: 1rem;">Welcome to our newsletter! This is your main content area where you can share updates, news, and valuable information with your audience.</p><p style="margin-bottom: 1.25rem; font-size: 1rem;">You can easily customize this template by editing the text, adding images, or including additional sections. The professional design ensures your message looks great on all devices.</p><p style="margin-bottom: 1.25rem; font-size: 1rem;">Feel free to add more paragraphs, lists, or any other content you need to communicate effectively with your readers.</p></div><div style="background-color: #34495e; padding: 2rem 2.5rem; color: #ecf0f1;"><div style="margin-bottom: 1.5rem;"><h3 style="margin: 0 0 1rem 0; color: #3498db; font-size: 1.1rem; font-weight: 600;">Contact Information</h3><p style="margin: 0.25rem 0; font-size: 0.9rem; line-height: 1.6;">Your Company Name<br>123 Business Street<br>City, State 12345<br>Email: info@yourcompany.com<br>Phone: (123) 456-7890</p></div><div style="border-top: 1px solid #4a5f7f; padding-top: 1.5rem; font-size: 0.85rem; color: #bdc3c7; text-align: center;"><p style="margin: 0;">&copy; 2024 Your Company. All rights reserved.</p><p style="margin: 0.5rem 0 0 0;"><a href="#" style="color: #3498db; text-decoration: none; margin: 0 0.5rem;">Unsubscribe</a> | <a href="#" style="color: #3498db; text-decoration: none; margin: 0 0.5rem;">Privacy Policy</a> | <a href="#" style="color: #3498db; text-decoration: none; margin: 0 0.5rem;">Terms of Service</a></p></div></div></div>`
      },
      {
        id: 'colorful',
        name: 'Colorful Sections',
        description: 'Bold template with black header, grey content, and blue footer',
        html: `<div style="max-width: 650px; margin: 0 auto; font-family: Arial, Helvetica, sans-serif; box-shadow: 0 4px 15px rgba(0,0,0,0.2);"><div style="background-color: #000000; padding: 3rem 2.5rem; text-align: center;"><h1 style="margin: 0; color: #ffffff; font-size: 2.25rem; font-weight: bold; text-transform: uppercase; letter-spacing: 2px;">Newsletter Title</h1><p style="margin: 1rem 0 0 0; color: #ffffff; font-size: 1rem; opacity: 0.9;">Your tagline or subtitle goes here</p></div><div style="padding: 3rem 2.5rem; background-color: #f5f5f5; color: #333333; line-height: 1.7;"><h2 style="color: #000000; font-size: 1.75rem; margin-top: 0; margin-bottom: 1.5rem; font-weight: bold;">Main Content Section</h2><p style="margin-bottom: 1.5rem; font-size: 1.05rem;">This grey section provides excellent contrast for your content. The neutral background makes text easy to read while maintaining a modern, professional look.</p><p style="margin-bottom: 1.5rem; font-size: 1.05rem;">You can add multiple paragraphs, images, lists, or any other content here. The grey background (#f5f5f5) is perfect for long-form content.</p><div style="background-color: #ffffff; padding: 1.5rem; margin: 2rem 0; border-left: 4px solid #1e3a8a; border-radius: 4px;"><p style="margin: 0; font-size: 1rem; color: #333333; font-style: italic;">"Add a highlighted quote or important message in this white box with a blue accent border."</p></div><p style="margin-bottom: 1.5rem; font-size: 1.05rem;">Continue with more content as needed. The template is fully customizable with inline CSS.</p></div><div style="background-color: #1e3a8a; padding: 2.5rem 2.5rem; color: #ffffff;"><div style="margin-bottom: 2rem;"><h3 style="margin: 0 0 1.25rem 0; color: #ffffff; font-size: 1.3rem; font-weight: bold; border-bottom: 2px solid #3b82f6; padding-bottom: 0.75rem;">Get In Touch</h3><p style="margin: 0.5rem 0; font-size: 1rem; line-height: 1.8;">Your Company Name<br>123 Business Avenue<br>New York, NY 10001<br><br><strong>Email:</strong> hello@yourcompany.com<br><strong>Phone:</strong> +1 (555) 123-4567<br><strong>Website:</strong> www.yourcompany.com</p></div><div style="border-top: 1px solid #3b82f6; padding-top: 2rem;"><div style="text-align: center; margin-bottom: 1.5rem;"><a href="#" style="display: inline-block; margin: 0 0.75rem; color: #ffffff; text-decoration: none; font-size: 1.5rem; width: 40px; height: 40px; line-height: 40px; background-color: #3b82f6; border-radius: 50%; text-align: center;">f</a><a href="#" style="display: inline-block; margin: 0 0.75rem; color: #ffffff; text-decoration: none; font-size: 1.5rem; width: 40px; height: 40px; line-height: 40px; background-color: #3b82f6; border-radius: 50%; text-align: center;">𝕏</a><a href="#" style="display: inline-block; margin: 0 0.75rem; color: #ffffff; text-decoration: none; font-size: 1.5rem; width: 40px; height: 40px; line-height: 40px; background-color: #3b82f6; border-radius: 50%; text-align: center;">in</a></div><p style="margin: 0; text-align: center; font-size: 0.9rem; color: #93c5fd;">&copy; 2024 Your Company. All rights reserved.</p><p style="margin: 0.75rem 0 0 0; text-align: center; font-size: 0.85rem;"><a href="#" style="color: #93c5fd; text-decoration: none; margin: 0 0.5rem;">Unsubscribe</a> • <a href="#" style="color: #93c5fd; text-decoration: none; margin: 0 0.5rem;">Privacy Policy</a> • <a href="#" style="color: #93c5fd; text-decoration: none; margin: 0 0.5rem;">Preferences</a></p></div></div></div>`
      }
    ];
  }

  getNewsletters() {
    return this.newsletters.asReadonly();
  }

  createNewsletter(title: string, content: string): Newsletter {
    const newsletter: Newsletter = {
      id: crypto.randomUUID(),
      title,
      content,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    this.newsletters.update(list => [...list, newsletter]);
    return newsletter;
  }

  updateNewsletter(id: string, title: string, content: string): void {
    this.newsletters.update(list =>
      list.map(n => n.id === id ? { ...n, title, content, updatedAt: new Date() } : n)
    );
  }

  getNewsletterById(id: string): Newsletter | undefined {
    return this.newsletters().find(n => n.id === id);
  }

  deleteNewsletter(id: string): void {
    this.newsletters.update(list => list.filter(n => n.id !== id));
  }

  getTemplates() {
    return this.templates.asReadonly();
  }

  getTemplateById(id: string): NewsletterTemplate | undefined {
    return this.templates().find(t => t.id === id);
  }

  saveAsTemplate(name: string, description: string, html: string): NewsletterTemplate {
    const template: NewsletterTemplate = {
      id: crypto.randomUUID(),
      name,
      description,
      html
    };
    this.templates.update(list => [...list, template]);
    return template;
  }
}

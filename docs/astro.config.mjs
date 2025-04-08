// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	site: 'https://resistorcat.github.io',
	base: 'cobbleloots',
	integrations: [
		starlight({
			title: 'Cobbleloots Docs',
			social: [
				{icon: 'github', label: 'GitHub', href: 'https://github.com/ResistorCat/cobbleloots'},
				{icon: 'discord', label: 'Discord', href: 'https://discord.gg/kbykWUH5dV'}
			],
			defaultLocale: 'root',
			locales: {
				root: {
					label: 'English',
					lang: 'en',
				},
				es: {
					label: 'Español',
					lang: 'es',
				}
			},
			logo: {
				src: '/src/assets/logo.png',
				alt: 'Cobbleloots Logo',
			},
			sidebar: [
				{
					label: 'Guides',
					translations: {
						es: 'Guías',
					},
					autogenerate: { directory: 'guides' },
				},
				{
					label: 'Reference',
					translations: {
						es: 'Referencia',
					},
					autogenerate: { directory: 'reference' },
				},
			],
		}),
	],
});

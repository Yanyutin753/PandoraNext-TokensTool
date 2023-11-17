module.exports = {
  plugins: [
    [
      'import',
      {
        libraryName: 'vant',
        libraryDirectory: 'es',
        style: true
      }
    ]
  ],
  presets: [
    '@vue/cli-plugin-babel/preset'
  ]
}
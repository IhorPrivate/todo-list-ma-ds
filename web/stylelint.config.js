export default {
  extends: ['stylelint-config-standard-scss'],
  rules: {
    'selector-class-pattern': ['^[a-z][a-zA-Z0-9]*$', { message: 'CSS Module class names are camelCase' }],
    'max-nesting-depth': [2, { ignore: ['pseudo-classes'] }],
    'selector-max-id': 0,
    'scss/no-global-function-names': true,
    'scss/at-rule-no-unknown': [true, { ignoreAtRules: ['container'] }],
  },
};

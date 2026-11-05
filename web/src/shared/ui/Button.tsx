import type { ComponentProps } from 'react';
import styles from './Button.module.scss';

type ButtonProps = ComponentProps<'button'> & {
  variant?: 'primary' | 'secondary';
};

export function Button({ variant = 'primary', type = 'button', className, ...buttonProps }: ButtonProps) {
  return (
    <button
      type={type}
      data-variant={variant}
      className={className === undefined ? styles.button : `${styles.button} ${className}`}
      {...buttonProps}
    />
  );
}
